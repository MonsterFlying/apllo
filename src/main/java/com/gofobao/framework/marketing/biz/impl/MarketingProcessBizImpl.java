package com.gofobao.framework.marketing.biz.impl;

import com.github.wenhao.jpa.PredicateBuilder;
import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.marketing.biz.MarketingProcessBiz;
import com.gofobao.framework.marketing.constans.MarketingTypeContants;
import com.gofobao.framework.marketing.entity.*;
import com.gofobao.framework.marketing.service.*;
import com.gofobao.framework.member.entity.UserCache;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

            log.info("活动时间刷选开始");
            filterMarketingByTime(marketings, marketingData);
            log.info("活动时间刷选结束");
        } catch (Exception e) {
            log.error("获取活动记录异常", e);
            return false;
        }

        try {
            log.info("筛选范围开始");
            filterDataByDimension(marketings, marketingData);
            log.info("筛选范围结束");
        } catch (Exception e) {
            log.error("筛选范围异常", e);
            return false;
        }

        // 筛选条件
        try {
            log.info("用户条件检测开始");
            filterDataByCondition(marketings, marketingData);
            log.info("用户条件检测结束");
        } catch (Exception e) {
            log.error("活动条件范围异常", e);
            return false;
        }

        if (CollectionUtils.isEmpty(marketings)) {
            log.info("红包派发, 用户参与活动为空");
            return true;
        }

        log.info("=========================");
        log.info("派发红包条件满足");
        log.info("=========================");
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
                return;
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
                        //Integer timeLimit = borrow.getTimeLimit();
                        // 查询标的信息
                        // tempMoney = validMoney * (timeLimit / 12D) * rule.getApr();
                        // money = tempMoney.longValue();
                        double principal = tender.getValidMoney(); // 投标金额
                        double apr = rule.getApr() * 100 * 100;  // 年化收益
                        BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(principal, apr, borrow.getTimeLimit(), nowDate);
                        Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
                        money = NumberHelper.toInt(calculatorMap.get("earnings"));  // 红包派发领取收益
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

        if (money <= 0) {
            log.error("红包金额为零, 不进行派发红包");
            return;
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
            log.info("红包派发成功");
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
            user = userService.findById(Long.parseLong(marketingData.getUserId()));
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
                        log.info("红包派发[登录时间未设置]");
                        iterator.remove();
                        continue;
                    }

                    Date updatedAt = user.getUpdatedAt();
                    if (DateHelper.diffInDays(updatedAt, loginMinTime, false) < 0) {
                        log.info("红包派发[登录时间不符合]");
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.TENDER:
                    if (!ObjectUtils.isEmpty(condition.getRegisterMinTime())) {
                        String userId = marketingData.getUserId();
                        Users users = userService.findById(Long.parseLong(userId));
                        if (ObjectUtils.isEmpty(users)) {
                            log.info("红包派发[用户不存在]");
                            continue;
                        }

                        Date createdAt = users.getCreatedAt();
                        if (condition.getRegisterMinTime().getTime() > createdAt.getTime()) {
                            log.info("红包派发[活动规定注册时间不符合]");
                            iterator.remove();
                            continue;
                        }
                    }


                    log.info("验证投标条件开始");
                    Long tenderMoneyMin = condition.getTenderMoneyMin();
                    if (ObjectUtils.isEmpty(tenderMoneyMin) || tenderMoneyMin <= 0) {
                        log.info("红包派发[投标金额不满住]");
                        iterator.remove();
                        continue;
                    }

                    Tender tender = tenderService.findById(Long.parseLong(marketingData.getSourceId()));
                    Preconditions.checkNotNull(tender, "MarketingProcessBizImpl.filterDataByCondition tender is null");
                    if (tender.getStatus().intValue() != 1) {
                        log.info("红包派发[标的状态未满足]");
                        iterator.remove();
                        continue;
                    }

                    Long validMoney = tender.getValidMoney();
                    if (validMoney < tenderMoneyMin) {
                        log.info("红包派发[小于规定投标金额]");
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.RECHARGE:
                    Long rechargeMoneyMin = condition.getRechargeMoneyMin();
                    if (ObjectUtils.isEmpty(rechargeMoneyMin) || rechargeMoneyMin <= 0) {
                        log.info("红包派发[充值金额不满足]");
                        iterator.remove();
                        continue;
                    }

                    RechargeDetailLog rechargeData = rechargeDetailLogService.findById(Long.parseLong(marketingData.getSourceId()));
                    Preconditions.checkNotNull(rechargeData, "MarketingProcessBizImpl.filterDataByCondition rechargeData is null");
                    if (rechargeData.getState() != 1) {
                        log.info("红包派发[充值状态属于未成功]");
                        iterator.remove();
                        continue;
                    }

                    if (rechargeData.getMoney() < rechargeMoneyMin) {
                        log.info("红包派发[充值金额不满足]");
                        iterator.remove();
                        continue;
                    }

                    break;
                case MarketingTypeContants.REGISTER:
                    Date registerMinTime = condition.getRegisterMinTime();
                    if (ObjectUtils.isEmpty(registerMinTime)) {
                        log.info("红包派发[未设置最小注册时间]");
                        iterator.remove();
                        continue;
                    }

                    Date createdAt = user.getCreatedAt();
                    if (DateHelper.diffInDays(createdAt, registerMinTime, false) < 0) {
                        log.info("红包派发[小于注册时间]");
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.OPEN_ACCOUNT:
                    Date openAccountMinTime = condition.getOpenAccountMinTime();
                    if (ObjectUtils.isEmpty(openAccountMinTime)) {
                        log.info("红包派发[开户时间未设置]");
                        iterator.remove();
                        continue;
                    }

                    UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(user.getId());
                    Preconditions.checkNotNull(userThirdAccount, "MarketingProcessBizImpl.filterDataByCondition rechargeData is null");
                    if (StringUtils.isEmpty(userThirdAccount.getAccountId())) {
                        log.info("红包派发[当前用户未开户]");
                        iterator.remove();
                        continue;
                    }

                    Date createAt = userThirdAccount.getCreateAt();
                    if (DateHelper.diffInDays(createAt, openAccountMinTime, false) < 0) {
                        log.info("红包派发[开户时间不匹配]");
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
        Gson gson = new Gson();
        String marketingDataStr = gson.toJson(marketingData);
        List<Long> marketingidList = marketings.stream().map(marketing -> marketing.getId()).collect(Collectors.toList());
        List<MarketingDimentsion> marketingDimentsionList = marketingDimentsionService.findBymarketingIdInAndDel(marketingidList, 0);
        Preconditions.checkNotNull(marketingDimentsionList, "MarketingProcessBizImpl.filterDataByDimension marketingDimentsionList is null ");
        Map<Long, MarketingDimentsion> dimentsionMap = marketingDimentsionList
                .stream()
                .collect(Collectors.toMap(MarketingDimentsion::getMarketingId, Function.identity()));

        //===============================
        // 屏蔽冻结用户
        //===============================
        Users user = null;
        try {
            user = userService.findById(Long.parseLong(marketingData.getUserId()));
            if (user.getIsLock()) {
                log.info("红包派发[用户处于冻结状态]");
                throw new Exception("MarketingProcessBizImpl.filterDataByDimension: user lock");
            }
        } catch (Exception e) {
            throw new Exception(e);
        }

        Iterator<Marketing> iterator = marketings.iterator();
        while (iterator.hasNext()) {
            Marketing marketing = iterator.next();
            log.info("活动信息" + marketing.getTitel());
            MarketingDimentsion marketingDimentsion = dimentsionMap.get(marketing.getId());
            Preconditions.checkNotNull(marketingDimentsion, "MarketingProcessBizImpl.filterDataByDimension marketingDimentsion is null");
            //============================
            //校验用户渠道问题
            //============================
            boolean channelState = verifyRegisterChannel(marketingDimentsion, user);
            if (!channelState) {
                log.info("红包派发[用户注册渠道不通过]");
                iterator.remove();
                continue;
            }
            boolean verifyParentState = verifyUserParent(marketingDimentsion, user);  // 验证是否被邀请
            if (!verifyParentState) {
                log.info("红包派发[邀请人判断不通过]");
                iterator.remove();
                continue;
            }

            boolean verifyUserNewState = verifyMemberType(marketingDimentsion, user, marketingData);  // 验证是否为新用户
            if (!verifyUserNewState) {
                log.info("红包派发[新老用户判断不通过]");
                iterator.remove();
                continue;
            }

            switch (marketingData.getMarketingType()) {
                case MarketingTypeContants.LOGIN:  // 登录
                    boolean loginPlatform = verifyUserPlatform(marketingDimentsion, user);
                    if (!loginPlatform) {
                        log.info("红包派发[登录平台判断不通过]");
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.TENDER:  // 投标
                    boolean tenderPlatformState = verifyTenderPlatform(marketingDimentsion, user, marketingData);
                    if (!tenderPlatformState) {
                        log.info("红包派发[投标平台判断不通过]");
                        iterator.remove();
                        continue;
                    }

                    boolean borrowTypeState = verifyBorrowType(marketingDimentsion, marketingData);
                    if (!borrowTypeState) {
                        log.info("红包派发[标的类型判断不通过]");
                        iterator.remove();
                        continue;
                    }
                    break;
                case MarketingTypeContants.RECHARGE:
                    // 处理充值来源
                    boolean rechargeSourceState = verifyRechargeSource(marketingDimentsion, marketingData);
                    if (!rechargeSourceState) {
                        log.info("红包派发[充值来源判断不通过]");
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
                        log.info("红包派发[开户来源判断不通过]");
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
        long tenderId = Long.parseLong(marketingData.getSourceId());
        Tender tender = tenderService.findById(tenderId);
        Preconditions.checkNotNull(tender, "MarketingProcessBizImpl.verifyBorrowType: tender not found");
        Borrow borrow = borrowService.findById(tender.getBorrowId());
        Preconditions.checkNotNull(borrow, "MarketingProcessBizImpl.verifyBorrowType: borrow not found");
        log.info("当前标的类型" + borrow.getType());
        if (borrowType.contains("-2")) {
            return borrow.getIsNovice();
        }

        String[] borrowTypeArr = borrowType.split(",");
        for (String item : borrowTypeArr) {
            int setType = Integer.parseInt(item);
            if (setType == borrow.getType().intValue()) {
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
        long tenderId = Long.parseLong(marketingData.getSourceId());
        Tender tender = tenderService.findById(tenderId);
        log.info("投标平台检测" + tender.getSource());
        if (tender.getIsAuto()) { // 自动投标
            return true;
        }

        for (String item : platformArr) {
            int source = tender.getSource();
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


    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 验证用户是否为新老用户
     *
     * @param marketingDimentsion
     * @param user
     * @param marketingData
     * @return
     */
    private boolean verifyMemberType(MarketingDimentsion marketingDimentsion, Users user, MarketingData marketingData) {
        if (marketingDimentsion.getMemberType().intValue() == 0) {
            return true;
        }
        String marketingType = marketingData.getMarketingType();
        boolean isNovice = true;
        if (MarketingTypeContants.TENDER.equals(marketingType)) {  // 投标验证新用户
            try {
                String sourceId = marketingData.getSourceId();
                long tenderId = Long.parseLong(sourceId);
                Tender tender = tenderService.findById(tenderId);
                String sqlStr = "SELECT  bt.*" +
                        "  FROM `gfb_borrow_tender` bt" +
                        "  INNER JOIN `gfb_borrow` b ON bt.`borrow_id`= b.`id`" +
                        " WHERE bt.`status`= 1" +
                        "   AND(b.`type`= 0" +
                        "    OR b.`type`= 4)" +
                        "   AND bt.`user_id`= :userId" +
                        "   AND bt.`borrow_id`  <>  :borrowId" +
                        "   AND bt.`created_at` < :createDate " +
                        "LIMIT 1 ";

                Query query = entityManager.createNativeQuery(sqlStr, Tender.class);
                query.setParameter("userId", tender.getUserId());
                query.setParameter("borrowId", tender.getBorrowId());
                query.setParameter("createDate", DateHelper.dateToString(tender.getCreatedAt()));
                List<Tender> tenders = query.getResultList();
                if (!CollectionUtils.isEmpty(tenders)) {
                    isNovice = false;  // 不是新手
                }
            } catch (NumberFormatException e) {
                log.error("查询新手用户异常", e);
                isNovice = false;
            }
        } else {
            UserCache userCache = userCacheService.findById(user.getId());
            isNovice = userCache.isNovice();
        }

        if (marketingDimentsion.getMemberType().intValue() == 2) {  //老用户
            return !isNovice;
        } else {  //新用户
            return isNovice;
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
                Users parentUser = userService.findById(user.getParentId());
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
            int source = user.getSource();
            for (String channel : channelArr) {
                int setChannel = 0;
                try {
                    setChannel = Integer.parseInt(channel);
                } catch (Exception e) {
                    log.error("验证用户渠道错误", e);
                    continue;
                }
                if (setChannel == source) {
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
            if (DateHelper.diffInDays(marketing.getEndTime(), DateHelper.stringToDate(marketingData.getTransTime()), false) < 0) {
                iterator.remove();
                log.info("红包派发: [活动已过期]");
                continue;
            }

            if (DateHelper.diffInDays(DateHelper.stringToDate(marketingData.getTransTime()), marketing.getBeginTime(), false) < 0) {
                log.info("红包派发: [活动未开始]");
                iterator.remove();
                continue;
            }
        }
    }

    /**
     * 活动缓存
     */

    public List<Marketing> getMarketing(String key) throws Exception {

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
            marketingData = gson.fromJson(marketingDataStr, MarketingData.class);  // 解析营销阐述
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


