package com.gofobao.framework.marketing.biz.impl;

import com.github.wenhao.jpa.PredicateBuilder;
import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.marketing.biz.MarketingProcessBiz;
import com.gofobao.framework.marketing.entity.Marketing;
import com.gofobao.framework.marketing.entity.MarketingCondition;
import com.gofobao.framework.marketing.entity.MarketingData;
import com.gofobao.framework.marketing.entity.MarketingDimentsion;
import com.gofobao.framework.marketing.service.*;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MarketingProcessBizImpl implements MarketingProcessBiz {

    @Autowired
    MarketingRedpackRecordService marketingRedpackRecordService;

    @Autowired
    MarketingRedpackRuleService marketingRedpackRuleService;

    @Autowired
    MarketingConditionService marketingConditionService;

    @Autowired
    MarketingService marketingService;

    @Autowired
    MarketingDimentsionService marketingDimentsionService;

    @Autowired
    UserService userService;

    @Autowired
    UserCacheService userCacheService;

    @Autowired
    TenderService tenderService;

    @Autowired
    BorrowService borrowService;

    @Autowired
    RechargeDetailLogService RechargeDetailLogService ;

    Gson gson = new Gson();

    @Override
    public boolean process(String marketingDataStr) {
        MarketingData marketingData = null;
        try {
            marketingData = parseMarketingData(marketingDataStr);   // 转换类型
        } catch (Exception e) {
            return false;
        }
        List<Marketing> marketings = null;
        try {
            marketings = findMarketing(marketingData);  // 获取活动
            if (CollectionUtils.isEmpty(marketings)) {
                log.info("MarketingProcessBizImpl.process marketing not found");
                return false;
            }
            // 筛选时间
            filterMarketingByTime(marketings, marketingData);
        } catch (Exception e) {
            return false;
        }

        // 筛选特定人群
        try {
            filterDataByDimension(marketings, marketingData);
        } catch (Exception e) {
            return false ;
        }


        return false;
    }


    /**
     * 根据活动的维度注过滤用户
     *
     * @param marketings
     * @param marketingData
     */
    private void filterDataByDimension(List<Marketing> marketings, MarketingData marketingData) throws Exception {
        List<Long> marketingidList = marketings.stream().map(marketing -> marketing.getId()).collect(Collectors.toList());
        List<MarketingDimentsion> marketingDimentsionList = marketingDimentsionService.findBymarketingIdInAndDel(marketingidList, 0);
        Preconditions.checkNotNull(marketingDimentsionList, "MarketingProcessBizImpl.filterDataByDimension marketingDimentsionList is null ");
        Map<Long, MarketingDimentsion> dimentsionMap = marketingDimentsionList
                .stream()
                .collect(Collectors.toMap(MarketingDimentsion::getMarketingId, Function.identity()));

        Users user = null;
        try {
            user = userService.findUserByUserId(marketingData.getUserId());
            if (user.getIsLock()) {
                throw new Exception("MarketingProcessBizImpl.filterDataByDimension: user islock");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

        Iterator<Marketing> iterator = marketings.iterator();
        while (iterator.hasNext()) {
            Marketing marketing = iterator.next();
            MarketingDimentsion marketingDimentsion = dimentsionMap.get(marketing.getId());
            Preconditions.checkNotNull(marketingDimentsion, "MarketingProcessBizImpl.filterDataByDimension marketingDimentsion is null");
            boolean channelState = verifyChannel(marketingDimentsion, marketingData, user);  // 验证用户渠道
            if (!channelState) {
                log.info("促销活动: 渠道用户验证不通过");
                iterator.remove();
                continue;
            }

            boolean verifyParentState = verifyUserParent(marketingDimentsion, user);  // 验证是否被邀请
            if (!verifyParentState) {
                log.info("促销活动: 当前用户不属于邀请用户");
                iterator.remove();
                continue;
            }

            boolean verifyUserNewState = verifyMemberType(marketingDimentsion, user);  // 验证是否为新用户
            if (!verifyUserNewState) {
                log.info("促销活动: 用户类型不符合");
                iterator.remove();
                continue;
            }

            switch (marketingData.getMarketingType()) {
                case LOGIN:
                    boolean loginPlatform = verifyUserPlatform(marketingDimentsion, user);
                    if (!loginPlatform) {
                        log.info("促销活动: 登录平台");
                        iterator.remove();
                        continue;
                    }
                    break;
                case TENDER:
                    boolean tenderPlatformState = verifyTenderPlatform(marketingDimentsion, user, marketingData);
                    if (!tenderPlatformState) {
                        log.info("促销活动: 投标平台不符合");
                        iterator.remove();
                        continue;
                    }

                    boolean borrowTypeState = verifyBorrowType(marketingDimentsion, marketingData);
                    if (!borrowTypeState) {
                        log.info("促销活动: 标的类型不符合");
                        iterator.remove();
                        continue;
                    }
                    break;
                case RECHARGE:
                    // 处理充值来源
                    boolean rechargeSourceState = verifyRechargeSource(marketingDimentsion, marketingData);
                    if(!rechargeSourceState){
                        log.info("促销活动: 充值来源");
                        iterator.remove();
                        continue;
                    }
                    break;
                case REGISTER:

                    break;
                case OPEN_ACCOUNT:

                    break;
                default:
                    throw new Exception("MarketingProcessBizImpl.filterDataByDimension: not found marketingType");
            }
        }
    }


    /**
     * 正对充值进行检测
     * @param marketingDimentsion
     * @param marketingData
     * @return
     */
    private boolean verifyRechargeSource(MarketingDimentsion marketingDimentsion, MarketingData marketingData) {
        String platform = marketingDimentsion.getPlatform();
        if(StringUtils.isEmpty(platform)){
            return true ;
        }

        RechargeDetailLog rachargeDetailLog = RechargeDetailLogService.findById(marketingData.getSourceId());
        Preconditions.checkNotNull(rachargeDetailLog, "MarketingProcessBizImpl.verifyRechargeSource: tender not found") ;
        if(rachargeDetailLog.getState() != 1){
            return false;
        }

        Integer rechargeSource = rachargeDetailLog.getRechargeSource();
        String[] platformArr = platform.split(",");
        for(String item: platformArr){
            if(Integer.parseInt(item) == rechargeSource){
                return true ;
            }
        }
        return false ;
    }

    /**
     * 验证标的类型
     *
     * @param marketingDimentsion
     * @param marketingData
     * @return
     */
    private boolean verifyBorrowType(MarketingDimentsion marketingDimentsion, MarketingData marketingData) {
        String borrowType = marketingDimentsion.getBorrowType();
        if (StringUtils.isEmpty(borrowType)) {
            return true;
        }
        Tender tender = tenderService.findById(marketingData.getSourceId());
        Preconditions.checkNotNull(tender, "MarketingProcessBizImpl.verifyBorrowType: tender not found");
        Borrow borrow = borrowService.findById(tender.getBorrowId());
        Preconditions.checkNotNull(borrow, "MarketingProcessBizImpl.verifyBorrowType: borrow not found");
        if (borrowType.contains("-2")) {
            return borrow.getIsVouch();
        }
        String[] borrowTypeArr = borrowType.split(",");
        for (String item : borrowTypeArr) {
            if (Integer.parseInt(item) == borrow.getType()) {
                return true;
            }
        }

        return false;

    }


    /**
     * 检测投标
     *
     * @param marketingDimentsion
     * @param user
     * @param marketingData
     * @return
     */
    private boolean verifyTenderPlatform(MarketingDimentsion marketingDimentsion, Users user, MarketingData marketingData) {
        String platform = marketingDimentsion.getPlatform();
        if (StringUtils.isEmpty(platform)) {
            return true;
        }

        String[] platformArr = platform.split(",");
        Long tenderId = marketingData.getSourceId();
        Tender tender = tenderService.findById(tenderId);
        if (tender.getIsAuto()) {
            return true;
        }

        for (String item : platformArr) {
            if (Integer.parseInt(item) == tender.getSource()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检测登录平台
     *
     * @param marketingDimentsion
     * @param user
     * @return
     */
    private boolean verifyUserPlatform(MarketingDimentsion marketingDimentsion, Users user) {
        Integer platform = user.getPlatform();
        String[] platformArr = marketingDimentsion.getPlatform().split(",");
        for (String item : platformArr) {
            if (Integer.parseInt(item) == platform) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证用户新老问题
     *
     * @param marketingDimentsion
     * @param user
     * @return
     */
    private boolean verifyMemberType(MarketingDimentsion marketingDimentsion, Users user) {
        if (marketingDimentsion.getMemberType() == 0) {
            return true;
        } else if (marketingDimentsion.getMemberType() == 1) {  // 新用户
            return userCacheService.isNew(user);
        } else { // 老用户
            return !userCacheService.isNew(user);
        }
    }

    /**
     * 验证是否需要邀请人
     *
     * @param marketingDimentsion
     * @param user
     * @return
     */
    private boolean verifyUserParent(MarketingDimentsion marketingDimentsion, Users user) {
        if (marketingDimentsion.getParentState() == 1) {
            if (!ObjectUtils.isEmpty(user.getParentId()) && user.getParentId() > 0) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证用户渠道
     *
     * @param marketingDimentsion
     * @param marketingData
     * @param user
     * @return
     */
    private boolean verifyChannel(MarketingDimentsion marketingDimentsion, MarketingData marketingData, Users user) {
        String channelType = marketingDimentsion.getChannelType();
        if (!StringUtils.isEmpty(channelType)) {
            String[] channelArr = channelType.split(",");
            Integer source = user.getSource();
            for (String channel : channelArr) {
                if (Integer.parseInt(channel) == source) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 根据活动时间筛选
     *
     * @param marketings
     * @param marketingData
     */
    private void filterMarketingByTime(Iterable<Marketing> marketings, MarketingData marketingData) {
        Iterator<Marketing> iterator = marketings.iterator();
        while (iterator.hasNext()) {
            Marketing marketing = iterator.next();
            if (DateHelper.diffInDays(marketing.getEndTime(), marketingData.getTransTime(), false) <= 0) {
                iterator.remove();
                continue;
            }

            if (DateHelper.diffInDays(marketingData.getTransTime(), marketing.getBeginTime(), false) <= 0) {
                iterator.remove();
                continue;
            }
        }
    }

    /**
     * 查找活动
     *
     * @param marketingData
     * @return
     */
    private List<Marketing> findMarketing(MarketingData marketingData) throws Exception {
        List<Marketing> marketings = Lists.newArrayList();
        // 查询投标金额大于零,且不删除
        Specification<MarketingCondition> marketingConditonSpecification = null;
        PredicateBuilder<MarketingCondition> builder = Specifications.
                <MarketingCondition>and()
                .eq("del", 0);

        switch (marketingData.getMarketingType()) {
            case LOGIN:
                builder.ne("loginMinTime", null);  // 登录时间不能为空
                break;

            case TENDER:
                builder.gt("tenderMoneyMin", 0);  // 投标金额大于零
                break;

            case RECHARGE:
                builder.gt("rechargeMoneyMin", 0); // 充值金额大于零
                break;

            case REGISTER:
                builder.ne("registerMinTime", null);  // 注册时间不能为空
                break;

            case OPEN_ACCOUNT:
                builder.ne("openAccountMinTime", null);  // 开户时间不能为空
                break;

            default:
                throw new Exception("MarketingProcessBizImpl.findMarketing: not found marketingType");
        }

        List<MarketingCondition> marketingConditions = marketingConditionService.findAll(marketingConditonSpecification);
        if (CollectionUtils.isEmpty(marketingConditions)) {
            return marketings;
        }
        List<Long> marketingIdList = marketingConditions
                .stream()
                .map(marketingCondition -> marketingCondition.getMarketingId())
                .collect(Collectors.toList());
        return marketingService.findByDelAndOpenStateAndBeginTimeGreaterThanEqualAndEndTimeLessThanEqualAndIdIn(0,
                1,
                marketingData.getTransTime(),
                marketingData.getTransTime(),
                marketingIdList);

    }

    /**
     * 解析营销参数
     *
     * @param marketingDataStr
     * @return
     */
    private MarketingData parseMarketingData(String marketingDataStr) throws Exception {
        MarketingData marketingData = null;
        try {
            marketingData = gson.fromJson(marketingDataStr, new TypeToken<MarketingData>() {
            }.getType());  // 解析营销阐述
        } catch (Exception e) {
            log.error("MarketingProcessBizImpl,parseMarketingData convert exception", e);
            throw new Exception(e);
        }

        Preconditions.checkNotNull(marketingData.getMarketingType(), "MarketingProcessBizImpl,parseMarketingData: marketingType null ");
        Preconditions.checkNotNull(marketingData.getUserId(), "MarketingProcessBizImpl,parseMarketingData: userId null ");
        Preconditions.checkNotNull(marketingData.getSourceId(), "MarketingProcessBizImpl,parseMarketingData: sourceId null ");
        Preconditions.checkNotNull(marketingData.getTransTime(), "MarketingProcessBizImpl,parseMarketingData: transTime null ");

        return marketingData;
    }
}
