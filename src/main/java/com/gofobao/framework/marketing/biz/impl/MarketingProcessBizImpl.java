package com.gofobao.framework.marketing.biz.impl;

import com.github.wenhao.jpa.PredicateBuilder;
import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.marketing.biz.MarketingProcessBiz;
import com.gofobao.framework.marketing.constans.MarketingTypeContants;
import com.gofobao.framework.marketing.entity.*;
import com.gofobao.framework.marketing.service.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Date;
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
    RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    Gson gson = new Gson();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean process(String marketingDataStr) throws Exception {
        log.info(String.format("活动监控:  %s", marketingDataStr));
        MarketingData marketingData = null;
        try {
            marketingData = parseMarketingData(marketingDataStr);   // 转换数据
        } catch (Exception e) {
            log.error("活动触发数据转换失败", e);
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
            log.error("获取活动记录异常", e);
            return false;
        }

        // 刷选范围
        try {
            filterDataByDimension(marketings, marketingData);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("筛选范围", e);
            return false;
        }

        // 筛选条件
        try {
            filterDataByCondition(marketings, marketingData);
        } catch (Exception e) {
            return false;
        }

        if (CollectionUtils.isEmpty(marketings)) {
            return true;
        }
        //  派发奖励
        for (Marketing marketing : marketings) {
            Integer marketingType = marketing.getMarketingType();
            switch (marketingType) {
                case 1:  // 红包
                    publishRedPack(marketing, marketingData);
                    break;
                case 2:  // 积分

                    break;
                case 3: // 现金券

                    break;
                default:
                    throw new Exception("当前奖励类型为空");
            }
        }

        return true;
    }

    /**
     * 派发红包
     */
    private void publishRedPack(Marketing marketing, MarketingData marketingData) throws Exception {
        Integer parentState = marketing.getParentState();
        // 判断派发为那个用户对象
        Users user = userService.findById(Long.parseLong(marketingData.getUserId()));
        Users opUser = null;
        if (1 == parentState) {
            if (user.getParentId() == 0) {
                log.error("红包派发: 当前用户父类为空");
                throw new Exception("红包派发: 当前用户父类为空");
            }
            opUser = userService.findById(new Long(user.getParentId()));
            Preconditions.checkNotNull(opUser, "MarketingProcessBizImpl.publishRedPack rule is null");
            if (opUser.getIsLock()) {
                log.error("红包派发: 当前用户被冻结");
                return;
            }
        } else {
            opUser = user;
        }

        MarketingRedpackRule rule = marketingRedpackRuleService.findTopByMarketingIdAndDel(marketing.getId(), 0);
        Preconditions.checkNotNull(rule, "MarketingProcessBizImpl.publishRedPack rule is null");
        Integer ruleType = rule.getRuleType();
        Date nowDate = new Date();
        MarketingRedpackRecord marketingRedpackRecord = new MarketingRedpackRecord();
        marketingRedpackRecord.setMarkeingTitel(marketing.getTitel());
        marketingRedpackRecord.setPublishTime(nowDate);
        marketingRedpackRecord.setCancelTime(DateHelper.addDays(nowDate, 60));
        marketingRedpackRecord.setRedpackRuleId(rule.getId());
        marketingRedpackRecord.setSourceId(Long.parseLong(marketingData.getSourceId()));
        long money = 0;
        StringBuffer remark = new StringBuffer();
        switch (marketingData.getMarketingType()) {
            case MarketingTypeContants.LOGIN:
                remark.append("登录奖励: ");
                // 红包类型: 1.投资金额随机百分比,2.投资金额规定百分比, 3.随机金额, 4.规定金额, 5.年化率
                switch (ruleType) {
                    case 1: // 投资金额随机百分比
                    case 2: // 投标金额百分比
                    case 5: // 年化率
                        throw new Exception("登录数据, 不具备投标类型奖励");
                    case 3: // 随机金额
                        remark.append("[随机奖励]");
                        money = getRandomMoney(rule.getMoneyMin(), rule.getMoneyMax());
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                    case 4: // 规定金额
                        remark.append("[固定奖励]");
                        money = rule.getMoneyMin();
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                }
                break;
            case MarketingTypeContants.TENDER:
                remark.append("投标奖励: ");
                Tender tender = tenderService.findById(Long.parseLong(marketingData.getSourceId()));
                Long validMoney = tender.getValidMoney();
                Double tempMoney;
                Double tenderMoneyMin;
                // 红包类型: 1.投资金额随机百分比,2.投资金额规定百分比, 3.随机金额, 4.规定金额, 5.年化率
                switch (ruleType) {
                    case 1: // 投资金额随机百分比
                        remark.append("[投标金额随机百分比]");
                        tenderMoneyMin = rule.getTenderMoneyMin();
                        Double tenderMoneyMax = rule.getTenderMoneyMax();
                        double randomMoney = tenderMoneyMax - tenderMoneyMin;
                        tempMoney = validMoney * (randomMoney + tenderMoneyMin);
                        money = tempMoney.longValue();
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                    case 2: // 投标金额百分比
                        tenderMoneyMin = rule.getTenderMoneyMin();
                        remark.append("[投标金额固定百分比]");
                        tempMoney = validMoney * tenderMoneyMin;
                        money = tempMoney.longValue();
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                    case 5: // 年化率
                        remark.append("[投标金额年化率]");
                        Long borrowId = tender.getBorrowId();
                        Borrow borrow = borrowService.findById(borrowId);
                        Integer timeLimit = borrow.getTimeLimit();
                        tempMoney = validMoney * (timeLimit / 12D) * rule.getApr();
                        money = tempMoney.longValue();
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                    case 3: // 随机金额
                        remark.append("[随机奖励]");
                        money = getRandomMoney(rule.getMoneyMin(), rule.getMoneyMax());
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                    case 4: // 规定金额
                        remark.append("[固定奖励]");
                        money = rule.getMoneyMin();
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                }
                break;
            case MarketingTypeContants.RECHARGE:
                remark.append("充值奖励: ");
                switch (ruleType) {
                    case 1:
                    case 2:
                    case 5:
                        throw new Exception("充值类型不支持此奖励类型");
                    case 3: // 随机金额
                        remark.append("[随机奖励]");
                        money = getRandomMoney(rule.getMoneyMin(), rule.getMoneyMax());
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                    case 4: // 规定金额
                        remark.append("[固定奖励]");
                        money = rule.getMoneyMin();
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                }
                break;
            case MarketingTypeContants.REGISTER:
                remark.append("注册奖励: ");
                switch (ruleType) {
                    case 1:
                    case 2:
                    case 5:
                        throw new Exception("注册类型不支持此奖励类型");
                    case 3: // 随机金额
                        remark.append("[随机奖励]");
                        money = getRandomMoney(rule.getMoneyMin(), rule.getMoneyMax());
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                    case 4: // 规定金额
                        remark.append("[固定奖励]");
                        money = rule.getMoneyMin();
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                }
                break;
            case MarketingTypeContants.OPEN_ACCOUNT:
                remark.append("开户奖励: ");
                switch (ruleType) {
                    case 1:
                    case 2:
                    case 5:
                        throw new Exception("开户类型不支持此奖励类型");
                    case 3: // 随机金额
                        remark.append("[随机奖励]");
                        money = getRandomMoney(rule.getMoneyMin(), rule.getMoneyMax());
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                    case 4: // 规定金额
                        remark.append("[固定奖励]");
                        money = rule.getMoneyMin();
                        remark.append(StringHelper.formatDouble(money / 100D, true));
                        remark.append("元");
                        break;
                }
                break;
            default:
                throw new Exception("MarketingProcessBizImpl.filterDataByDimension: not found marketingType");
        }
        marketingRedpackRecord.setMarketingId(marketing.getId());
        marketingRedpackRecord.setUserId(opUser.getId());
        marketingRedpackRecord.setMoney(money);
        marketingRedpackRecord.setRemark(remark.toString());
        // 查询记录是否存在
        List<MarketingRedpackRecord> marketingRedpackRecords = marketingRedpackRecordService.findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(marketingRedpackRecord.getMarketingId(),
                marketingRedpackRecord.getRedpackRuleId(),
                marketingRedpackRecord.getUserId(),
                marketingRedpackRecord.getSourceId());

        if (!CollectionUtils.isEmpty(marketingRedpackRecords)) {
            log.error("红包被重复派发");
        } else {
            marketingRedpackRecordService.save(marketingRedpackRecord);
        }
    }

    /**
     * 获取规定金额
     *
     * @return
     */
    private long getRandomMoney(Long minMoney, Long maxMoney) {
        SecureRandom secureRandom = new SecureRandom();
        int randomValue = secureRandom.nextInt(maxMoney.intValue() - minMoney.intValue());
        return minMoney + randomValue;
    }

    /**
     * s筛选条件
     *
     * @param marketings
     * @param marketingData
     */
    private void filterDataByCondition(List<Marketing> marketings, MarketingData marketingData) throws Exception {
        List<Long> marketingidList = marketings.stream().map(marketing -> marketing.getId()).collect(Collectors.toList());
        List<MarketingCondition> marketingConditions = marketingConditionService.findBymarketingIdInAndDel(marketingidList, 0);
        Preconditions.checkNotNull(marketingConditions, "MarketingProcessBizImpl.filterDataByCondition marketingConditions is null");
        Map<Long, MarketingCondition> conditionMap = marketingConditions
                .stream()
                .collect(Collectors.toMap(MarketingCondition::getMarketingId, Function.identity()));

        Users user = null;
        try {
            user = userService.findUserByUserId(Long.parseLong(marketingData.getUserId()));
            if (user.getIsLock()) {
                throw new Exception("MarketingProcessBizImpl.filterDataByCondition: user lock");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

        Iterator<Marketing> iterator = marketings.iterator();
        while (iterator.hasNext()) {
            Marketing marketing = iterator.next();
            MarketingCondition condition = conditionMap.get(marketing.getId());
            switch (marketingData.getMarketingType()) {
                case MarketingTypeContants.LOGIN:
                    Date loginMinTime = condition.getLoginMinTime();
                    if (ObjectUtils.isEmpty(loginMinTime)) {
                        iterator.remove();
                        continue;
                    }

                    Date updatedAt = user.getUpdatedAt();
                    if (DateHelper.diffInDays(updatedAt, loginMinTime, false) < 0) {
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.TENDER:
                    Long tenderMoneyMin = condition.getTenderMoneyMin();
                    if (ObjectUtils.isEmpty(tenderMoneyMin) || tenderMoneyMin <= 0) {
                        iterator.remove();
                        continue;
                    }

                    Tender tender = tenderService.findById(Long.parseLong(marketingData.getSourceId()));
                    Preconditions.checkNotNull(tender, "MarketingProcessBizImpl.filterDataByCondition tender is null");
                    if (tender.getState() != 1) {
                        iterator.remove();
                        continue;
                    }

                    Long validMoney = tender.getValidMoney();
                    if (validMoney < tenderMoneyMin) {
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.RECHARGE:
                    Long rechargeMoneyMin = condition.getRechargeMoneyMin();
                    if (ObjectUtils.isEmpty(rechargeMoneyMin) || rechargeMoneyMin <= 0) {
                        iterator.remove();
                        continue;
                    }

                    RechargeDetailLog rechargeData = rechargeDetailLogService.findById(Long.parseLong(marketingData.getSourceId()));
                    Preconditions.checkNotNull(rechargeData, "MarketingProcessBizImpl.filterDataByCondition rechargeData is null");
                    if (rechargeData.getState() != 1) {
                        iterator.remove();
                        continue;
                    }

                    if (rechargeData.getMoney() < rechargeMoneyMin) {
                        iterator.remove();
                        continue;
                    }

                    break;
                case MarketingTypeContants.REGISTER:
                    Date registerMinTime = condition.getRegisterMinTime();
                    if (ObjectUtils.isEmpty(registerMinTime)) {
                        iterator.remove();
                        continue;
                    }

                    Date createdAt = user.getCreatedAt();
                    if (DateHelper.diffInDays(createdAt, registerMinTime, false) < 0) {
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.OPEN_ACCOUNT:
                    Date openAccountMinTime = condition.getOpenAccountMinTime();
                    if (ObjectUtils.isEmpty(openAccountMinTime)) {
                        iterator.remove();
                        continue;
                    }

                    UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
                    Preconditions.checkNotNull(userThirdAccount, "MarketingProcessBizImpl.filterDataByCondition rechargeData is null");
                    if (StringUtils.isEmpty(userThirdAccount.getAccountId())) {
                        iterator.remove();
                        continue;
                    }

                    Date createAt = userThirdAccount.getCreateAt();
                    if (DateHelper.diffInDays(createAt, openAccountMinTime, false) < 0) {
                        iterator.remove();
                        continue;
                    }
                    break;
                default:
                    throw new Exception("MarketingProcessBizImpl.filterDataByDimension: not found marketingType");
            }
        }
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
            user = userService.findUserByUserId(Long.parseLong(marketingData.getUserId()));
            if (user.getIsLock()) {
                throw new Exception("MarketingProcessBizImpl.filterDataByDimension: user lock");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

        Iterator<Marketing> iterator = marketings.iterator();
        while (iterator.hasNext()) {
            Marketing marketing = iterator.next();
            MarketingDimentsion marketingDimentsion = dimentsionMap.get(marketing.getId());
            Preconditions.checkNotNull(marketingDimentsion, "MarketingProcessBizImpl.filterDataByDimension marketingDimentsion is null");
            boolean channelState = verifyRegisterChannel(marketingDimentsion, user);  // 验证用户注册渠道
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
                case MarketingTypeContants.LOGIN:  // 登录
                    boolean loginPlatform = verifyUserPlatform(marketingDimentsion, user);
                    if (!loginPlatform) {
                        log.info("促销活动: 登录平台");
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.TENDER:  // 投标
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
                case MarketingTypeContants.RECHARGE:
                    // 处理充值来源
                    boolean rechargeSourceState = verifyRechargeSource(marketingDimentsion, marketingData);
                    if (!rechargeSourceState) {
                        log.info("促销活动: 充值来源");
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.REGISTER:
                    // 注册不验证用户platform
                    break;

                case MarketingTypeContants.OPEN_ACCOUNT:
                    boolean openAccountState = verifyOpenAccountSource(marketingDimentsion, marketingData);
                    if (!openAccountState) {
                        log.info("促销活动: 开户来源");
                        iterator.remove();
                        continue;
                    }
                    break;
                default:
                    throw new Exception("MarketingProcessBizImpl.filterDataByDimension: not found marketingType");
            }
        }
    }

    // 验证开户流程
    private boolean verifyOpenAccountSource(MarketingDimentsion marketingDimentsion, MarketingData marketingData) {
        String platform = marketingDimentsion.getPlatform();
        if (StringUtils.isEmpty(platform)) {
            return true;
        }

        Users user = userService.findById(Long.parseLong(marketingData.getUserId()));
        Preconditions.checkNotNull(user, "MarketingProcessBizImpl.verifyOpenAccountSource: user not found");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
        Preconditions.checkNotNull(userThirdAccount, "MarketingProcessBizImpl.verifyOpenAccountSource: userThirdAccount not found");
        if (StringUtils.isEmpty(userThirdAccount.getAccountId())) {
            return false;
        }

        return true;
    }


    /**
     * 验证用户注册
     *
     * @param marketingDimentsion
     * @param marketingData
     * @return
     */
    private boolean verifyRegisterSource(MarketingDimentsion marketingDimentsion, MarketingData marketingData) {
        String platform = marketingDimentsion.getPlatform();
        if (StringUtils.isEmpty(platform)) {
            return true;
        }

        Users user = userService.findById(Long.parseLong(marketingData.getSourceId()));
        Preconditions.checkNotNull(user, "MarketingProcessBizImpl.verifyRegisterSource: user not found");
        Integer source = user.getSource();
        String[] platformArr = platform.split(",");
        for (String item : platformArr) {
            if (Integer.parseInt(item) == source) {
                return true;
            }
        }
        return false;
    }

    /**
     * 正对充值进行检测
     *
     * @param marketingDimentsion
     * @param marketingData
     * @return
     */
    private boolean verifyRechargeSource(MarketingDimentsion marketingDimentsion, MarketingData marketingData) {
        String platform = marketingDimentsion.getPlatform();
        if (StringUtils.isEmpty(platform)) {
            return true;
        }

        RechargeDetailLog rachargeDetailLog = rechargeDetailLogService.findById(Long.parseLong(marketingData.getSourceId()));
        Preconditions.checkNotNull(rachargeDetailLog, "MarketingProcessBizImpl.verifyRechargeSource: tender not found");
        if (rachargeDetailLog.getState() != 1) {
            log.info("充值检测: 充值状态失败");
            return false;
        }

        Integer rechargeSource = rachargeDetailLog.getRechargeSource();
        String[] platformArr = platform.split(",");
        for (String item : platformArr) {
            if (Integer.parseInt(item) == rechargeSource) {
                return true;
            }
        }
        return false;
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
        Tender tender = tenderService.findById(Long.parseLong(marketingData.getSourceId()));
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
        Long tenderId = Long.parseLong(marketingData.getSourceId());
        Tender tender = tenderService.findById(tenderId);
        if (tender.getIsAuto()) { // 自动投标
            return true;
        }

        for (String item : platformArr) {
            Integer source = tender.getSource();
            int i = Integer.parseInt(item);
            if ((!ObjectUtils.isEmpty(source)) && (i == source)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检测用户登录平台
     * 0: PC登录
     * 1: android登录
     * 2: ios登录
     * 3: html登录
     *
     * @param marketingDimentsion
     * @param user
     * @return
     */
    private boolean verifyUserPlatform(MarketingDimentsion marketingDimentsion, Users user) {
        Integer platform = user.getPlatform();
        String platformStr = marketingDimentsion.getPlatform();
        if (StringUtils.isEmpty(platformStr)) {
            return true;
        }

        String[] platformArr = marketingDimentsion.getPlatform().split(",");
        for (String item : platformArr) {
            if (Integer.parseInt(item) == platform) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证用户是否为新老用户
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
     * 判断用户是否为被邀请用户
     * 注意:
     * 程序会检测邀请用户是否被冻结
     *
     * @param marketingDimentsion
     * @param user
     * @return
     */
    private boolean verifyUserParent(MarketingDimentsion marketingDimentsion, Users user) {
        if (marketingDimentsion.getParentState().intValue() == 1) {
            if (!ObjectUtils.isEmpty(user.getParentId()) && user.getParentId() > 0) {
                Users parentUser = userService.findUserByUserId(user.getParentId());
                if (ObjectUtils.isEmpty(parentUser) || parentUser.getIsLock()) {
                    log.info("判断用户是否为被邀请用户: 邀请用户不存在/邀请用户被冻结");
                    return false;
                }
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证用户注册渠道
     * 0: pc 用户
     * 1: android 用户
     * 2: ios 用户
     * 9: html 用户
     *
     * @param marketingDimentsion
     * @param user
     * @return
     */
    private boolean verifyRegisterChannel(MarketingDimentsion marketingDimentsion, Users user) {
        String channelType = marketingDimentsion.getChannelType();
        if (!StringUtils.isEmpty(channelType)) {
            String[] channelArr = channelType.split(",");
            Integer source = user.getSource();
            for (String channel : channelArr) {
                if (Integer.parseInt(channel) == source) {
                    return true;
                }
            }
            return false;
        }

        return true;
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
            if (DateHelper.diffInDays(marketing.getEndTime(), DateHelper.stringToDate(marketingData.getTransTime()), false) <= 0) {
                iterator.remove();
                continue;
            }

            if (DateHelper.diffInDays(DateHelper.stringToDate(marketingData.getTransTime()), marketing.getBeginTime(), false) <= 0) {
                iterator.remove();
                continue;
            }
        }
    }

    /**
     * 活动缓存
     */
   /* LoadingCache<String, List<Marketing>> marketingConditonCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .build(new CacheLoader<String, List<Marketing>>() {
                @Override
                public List<Marketing> load(String key) throws Exception {
                    // 查询投标金额大于零,且不删除
                    PredicateBuilder<MarketingCondition> builder = Specifications.
                            <MarketingCondition>and()
                            .eq("del", 0); // 没有删除
                    switch (key) {
                        case MarketingTypeContants.LOGIN:
                            builder.ne("loginMinTime", null);  // 登录时间不能为空
                            break;
                        case MarketingTypeContants.TENDER:
                            builder.gt("tenderMoneyMin", 0);  // 投标金额大于零
                            break;
                        case MarketingTypeContants.RECHARGE:
                            builder.gt("rechargeMoneyMin", 0); // 充值金额大于零
                            break;
                        case MarketingTypeContants.REGISTER:
                            builder.ne("registerMinTime", null);  // 注册时间不能为空
                            break;
                        case MarketingTypeContants.OPEN_ACCOUNT:
                            builder.ne("openAccountMinTime", null);  // 开户时间不能为空
                            break;
                        default:
                            throw new Exception("MarketingProcessBizImpl.findMarketing: not found marketingType");
                    }
                    Specification<MarketingCondition> marketingConditonSpecification = builder.build();
                    List<MarketingCondition> marketingConditions =
                            marketingConditionService.findAll(marketingConditonSpecification);

                    if (CollectionUtils.isEmpty(marketingConditions)) {
                        log.info("根据活动数据查询活动条件记录为空");
                        return Lists.newArrayList();
                    }

                    List<Long> marketingIdList = marketingConditions
                            .stream()
                            .map(marketingCondition -> marketingCondition.getMarketingId())
                            .collect(Collectors.toList());
                    return marketingService.findByDelAndOpenStateAndIdIn(0,
                            1,
                            marketingIdList);
                }
            });*/


    public List<Marketing> getMarketing(String key)throws Exception{

        PredicateBuilder<MarketingCondition> builder = Specifications.
                <MarketingCondition>and()
                .eq("del", 0); // 没有删除
        switch (key) {
            case MarketingTypeContants.LOGIN:
                builder.ne("loginMinTime", null);  // 登录时间不能为空
                break;
            case MarketingTypeContants.TENDER:
                builder.gt("tenderMoneyMin", 0);  // 投标金额大于零
                break;
            case MarketingTypeContants.RECHARGE:
                builder.gt("rechargeMoneyMin", 0); // 充值金额大于零
                break;
            case MarketingTypeContants.REGISTER:
                builder.ne("registerMinTime", null);  // 注册时间不能为空
                break;
            case MarketingTypeContants.OPEN_ACCOUNT:
                builder.ne("openAccountMinTime", null);  // 开户时间不能为空
                break;
            default:
                throw new Exception("MarketingProcessBizImpl.findMarketing: not found marketingType");
        }
        Specification<MarketingCondition> marketingConditonSpecification = builder.build();
        List<MarketingCondition> marketingConditions =
                marketingConditionService.findAll(marketingConditonSpecification);

        if (CollectionUtils.isEmpty(marketingConditions)) {
            log.info("根据活动数据查询活动条件记录为空");
            return Lists.newArrayList();
        }

        List<Long> marketingIdList = marketingConditions
                .stream()
                .map(marketingCondition -> marketingCondition.getMarketingId())
                .collect(Collectors.toList());
        return marketingService.findByDelAndOpenStateAndIdIn(0,
                1,
                marketingIdList);
    }




    //  Map<String,Integer> marketingMap= ImmutableMap.of("OPEN_ACCOUNT",1,"TENDER",2);


    /**
     * 查找活动
     *
     * @param marketingData
     * @return
     */
    private List<Marketing> findMarketing(MarketingData marketingData) throws Exception {
        return getMarketing(marketingData.getMarketingType());
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
            marketingData = gson.fromJson(marketingDataStr,  MarketingData.class);  // 解析营销阐述
        } catch (Exception e) {
            log.error("MarketingProcessBizImpl,parseMarketingData convert exception", e);
            throw new Exception(e);
        }

        Preconditions.checkNotNull(marketingData.getMarketingType(), "MarketingProcessBizImpl.parseMarketingData: marketingType null ");
        Preconditions.checkNotNull(marketingData.getUserId(), "MarketingProcessBizImpl.parseMarketingData: userId null ");
        Preconditions.checkNotNull(marketingData.getSourceId(), "MarketingProcessBizImpl.parseMarketingData: sourceId null ");
        Preconditions.checkNotNull(marketingData.getTransTime(), "MarketingProcessBizImpl.parseMarketingData: transTime null ");
        return marketingData;
    }
}


