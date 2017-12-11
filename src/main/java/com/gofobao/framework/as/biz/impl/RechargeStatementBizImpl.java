package com.gofobao.framework.as.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Item;
import com.gofobao.framework.as.biz.RechargeStatementBiz;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.RechargeDetailLogService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.project.QueryThirdRecordHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author Administrator
 */
@Component
@Slf4j
public class RechargeStatementBizImpl implements RechargeStatementBiz {

    @Autowired
    private RechargeDetailLogService rechargeDetailLogService;

    @Autowired
    AssetChangeProvider assetChangeProvider;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    NewEveService newEveService;

    @Autowired
    NewAleveService newAleveService;

    @Autowired
    JixinManager jixinManager;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    MqHelper mqHelper;

    @Autowired
    QueryThirdRecordHelper queryThirdRecordHelper ;

    /**
     * 离线匹配账单
     * 只能匹配昨天以后的账目
     * 1. 查询该用户指定某天的所有第三方充值记录
     * 2. 查询该用户所有充值记录
     * 3. 逐个匹配金额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offlineStatement(Long userId, Date date, RechargeStatementBizImpl.RechargeType rechargeType) throws Exception {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "userthirdAccount record is null");
        Preconditions.checkNotNull(date, "date is null");
        List<NewEve> thirdRechargeRecords = null;
        try {
            String type = rechargeType.getLocalType();
            thirdRechargeRecords = newEveService.findAllByTranTypeAndDateAndAccountId(type, userThirdAccount.getAccountId(), date);
        } catch (Exception e) {
            log.warn("对账: 查询线下充值记录异常", e);
            return false;
        }

        List<RechargeDetailLog> rechargeDetailLogs = findLocalRechargeRecord(userThirdAccount, date, rechargeType);
        if (CollectionUtils.isEmpty(rechargeDetailLogs)) {
            log.warn("对账:查询本地流水为空");
        }

        if (CollectionUtils.isEmpty(thirdRechargeRecords) && CollectionUtils.isEmpty(rechargeDetailLogs)) {
            log.warn("存管流水与本地流水相持平, 无需进行对账");
            return true;
        }

        // 匹配正确的
        return doOfflineMacthRecord(userThirdAccount, date, thirdRechargeRecords, rechargeDetailLogs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean onlineStatement(Long userId, Date date, RechargeStatementBizImpl.RechargeType rechargeType, boolean force) throws Exception {
        // 判断时间是否为当天
        Date nowDate = new Date();
        if (DateHelper.diffInDays(nowDate, date, false) != 0) {
            log.error("[实时对账] 实时数据查询只支持当天数据查询");
            return false;
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "userthirdAccount record is null");
        Preconditions.checkNotNull(date, "date is null");
        List<AccountDetailsQuery2Item> thirdRechargeRecordList = null;
        try {
            log.info("[实时对账] 实时查询即信流水记录");
            thirdRechargeRecordList = queryThirdRecordHelper.queryThirdRecord(userThirdAccount.getAccountId(), date, rechargeType.getLocalType()) ;
        } catch (Exception e) {
            return false ;
        }

        List<RechargeDetailLog> localRechargeRecordList = findLocalRechargeRecord(userThirdAccount, date, rechargeType);
        if (CollectionUtils.isEmpty(localRechargeRecordList)) {
            log.warn("[实时对账] 查询本地流水为空");
        }

        return doOnlineMacthRecord(userThirdAccount, date, thirdRechargeRecordList, localRechargeRecordList, force);
    }


    /**
     * 实时充值匹配
     *
     * @param userThirdAccount
     * @param date
     * @param thirdRechargeRecordList
     * @param localRechargeRecordList
     * @param force                   是否前置执行对账
     * @return
     */
    private boolean doOnlineMacthRecord(UserThirdAccount userThirdAccount,
                                        Date date,
                                        List<AccountDetailsQuery2Item> thirdRechargeRecordList,
                                        List<RechargeDetailLog> localRechargeRecordList,
                                        boolean force) throws Exception {

        // 对即信流水进行分类
        // 拨正数据集合
        List<AccountDetailsQuery2Item> errorThirdRechargeRecords = new ArrayList<>();
        // 正确数据集合
        List<AccountDetailsQuery2Item> okThirdRechargeRecords = new ArrayList<>();
        for (AccountDetailsQuery2Item item : thirdRechargeRecordList) {
            if ("R".equalsIgnoreCase(item.getOrFlag())) {
                errorThirdRechargeRecords.add(item);
            } else {
                okThirdRechargeRecords.add(item);
            }
        }

        if (CollectionUtils.isEmpty(errorThirdRechargeRecords)) {
            log.warn("[实时对账] 存在保证充值记录");
        }

        // 对本地流水进行分类
        List<RechargeDetailLog> errorLocalRechargeDetailRecords = new ArrayList<>();
        List<RechargeDetailLog> okLocalRechargeDetailRecords = new ArrayList<>();
        for (RechargeDetailLog item : localRechargeRecordList) {
            String state = item.getState().toString();
            if ("1".equalsIgnoreCase(state)) {
                // 充值成功
                okLocalRechargeDetailRecords.add(item);
            } else if ("0".equalsIgnoreCase(state)) {
                // 待确认流水
                throw new Exception("[实时充值对账]: 存在待确认充值记录");
            } else {
                // 充值失败
                errorLocalRechargeDetailRecords.add(item);
            }
        }

        Iterator<AccountDetailsQuery2Item> okThirdIterator = okThirdRechargeRecords.iterator();
        while (okThirdIterator.hasNext()) {
            // 即信流水对象
            AccountDetailsQuery2Item accountDetailsQueryItem = okThirdIterator.next();
            Iterator<RechargeDetailLog> okLocalIterator = okLocalRechargeDetailRecords.iterator();
            while (okLocalIterator.hasNext()) {
                // 本地流水对象
                RechargeDetailLog rechargeDetailLog = okLocalIterator.next();
                long eveMoney = MoneyHelper.yuanToFen(accountDetailsQueryItem.getTxAmount());
                if (eveMoney == rechargeDetailLog.getMoney()) {
                    log.info(String.format("[实时充值对账] 核对成功 金额: %s", eveMoney));
                    okLocalIterator.remove();
                    okThirdIterator.remove();
                    break;
                }
            }
        }

        // 针对即信流水剩余, 进行补单
        if (!CollectionUtils.isEmpty(okThirdRechargeRecords)) {
            log.warn("[实时对账] 执行充值补单操作");
            okThirdIterator = okThirdRechargeRecords.iterator();
            // 充值失败的迭代器
            while (okThirdIterator.hasNext()) {
                // 即信流水
                Iterator<RechargeDetailLog> errorLocalIterator = errorLocalRechargeDetailRecords.iterator();
                AccountDetailsQuery2Item item = okThirdIterator.next();
                while (errorLocalIterator.hasNext()) {
                    // 本地流水
                    RechargeDetailLog rechargeDetailLog = errorLocalIterator.next();
                    long eveMoney = MoneyHelper.yuanToFen(item.getTxAmount());
                    if (eveMoney == rechargeDetailLog.getMoney()) {
                        log.info(String.format("[实时充值对账] 找回充值记录 金额: %s", eveMoney));
                        if (force) {
                            boolean updateState = updateRechargeRecord(rechargeDetailLog);
                            String msg = GSON.toJson(rechargeDetailLog);
                            exceptionEmailHelper.sendErrorMessage(
                                    String.format("[实时充值对账] 找回充值记录 %s", updateState ? "成功" : "失败"),
                                    msg);
                        } else {
                            log.warn("未开启强制对账, 取消补单操作");
                        }

                        errorLocalIterator.remove();
                        okThirdIterator.remove();
                        break;
                    }
                }
            }

            if (!CollectionUtils.isEmpty(okThirdRechargeRecords)) {
                for (AccountDetailsQuery2Item item : okThirdRechargeRecords) {
                    if (force) {
                        boolean insertState = insertRechargeRecordByOnline(item, userThirdAccount);
                        String msg = GSON.toJson(item);
                        exceptionEmailHelper.sendErrorMessage(
                                String.format("[对账] 找回充值记录 %s", insertState ? "成功" : "失败"),
                                msg);
                    } else {
                        log.warn("未开启强制对账, 取消补单操作");
                    }

                }
            }
        }


        // 针对本地充值流水剩余, 进行拨正
        if (!CollectionUtils.isEmpty(okLocalRechargeDetailRecords)) {
            log.warn("[实时对账] 执行充值拨正操作");
            for (RechargeDetailLog item : okLocalRechargeDetailRecords) {
                boolean cancelState = cancelRecord(item);
                String msg = GSON.toJson(item);
                exceptionEmailHelper.sendErrorMessage(
                        String.format("[实时对账] 取消充值记录 %s", cancelState ? "成功" : "失败"),
                        msg);
            }
        }
        return true;
    }

    /**
     * 实时对账补单
     *
     * @param item
     * @param userThirdAccount
     * @return
     */
    private boolean insertRechargeRecordByOnline(AccountDetailsQuery2Item item, UserThirdAccount userThirdAccount) throws Exception {
        String transtype = item.getTranType();
        AssetChangeTypeEnum assetChangeTypeEnum = null;
        try {
            assetChangeTypeEnum = AssetChangeTypeEnum.findByRemoteType(transtype);
        } catch (Exception e) {
            log.error(" [实时充值对账] 根据即信资金变动类型查询本地变动记录", e);
        }

        Date nowDate = new Date();
        // 插入充值记录
        String seqNo = String.format("%s%s", DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMDHMS_NUM), item.getTraceNo());
        RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
        //充值类型
        rechargeDetailLog.setRechargeType(AssetChangeTypeEnum.offlineRecharge.equals(assetChangeTypeEnum) ? 1 : 0);
        rechargeDetailLog.setUserId(userThirdAccount.getUserId());
        rechargeDetailLog.setBankName(userThirdAccount.getBankName());
        rechargeDetailLog.setCallbackTime(nowDate);
        rechargeDetailLog.setCreateTime(nowDate);
        rechargeDetailLog.setUpdateTime(nowDate);
        rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
        rechargeDetailLog.setDel(0);
        rechargeDetailLog.setIp("127.0.0.1");
        rechargeDetailLog.setMobile(userThirdAccount.getMobile());
        rechargeDetailLog.setMoney(MoneyHelper.yuanToFen(item.getTxAmount()));
        rechargeDetailLog.setRechargeChannel(0);
        rechargeDetailLog.setState(1);
        rechargeDetailLog.setSeqNo(seqNo);
        rechargeDetailLog.setResponseMessage("正常补单");
        rechargeDetailLogService.save(rechargeDetailLog);

        // 充值成功
        AssetChange entity = new AssetChange();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        entity.setGroupSeqNo(groupSeqNo);
        entity.setMoney(rechargeDetailLog.getMoney());
        entity.setSeqNo(seqNo);
        entity.setUserId(userThirdAccount.getUserId());
        entity.setRemark(String.format("你在 %s 成功充值%s元", DateHelper.dateToString(nowDate), item.getTxAmount()));
        entity.setType(assetChangeTypeEnum);
        assetChangeProvider.commonAssetChange(entity);
        return true;
    }


    /**
     * 核对记录
     *
     * @param userThirdAccount
     * @param date
     * @param thirdRechargeRecords
     * @param rechargeDetailLogs
     * @return
     */
    public boolean doOfflineMacthRecord(UserThirdAccount userThirdAccount,
                                        Date date,
                                        List<NewEve> thirdRechargeRecords,
                                        List<RechargeDetailLog> rechargeDetailLogs) throws Exception {
        // 对即信流水进行分类
        List<NewEve> errorThirdRechargeRecords = new ArrayList<>(); // 拨正数据集合
        List<NewEve> okThirdRechargeRecords = new ArrayList<>(); // 正确数据集合
        for (NewEve item : thirdRechargeRecords) {
            if ("1".equalsIgnoreCase(item.getErvind())) {
                errorThirdRechargeRecords.add(item);
            } else {
                okThirdRechargeRecords.add(item);
            }
        }

        // 对本地流水进行分类
        List<RechargeDetailLog> errorLocalRechargeDetailRecords = new ArrayList<>();
        List<RechargeDetailLog> okLocalRechargeDetailRecords = new ArrayList<>();
        for (RechargeDetailLog item : rechargeDetailLogs) {
            String state = item.getState().toString();
            if ("1".equalsIgnoreCase(state)) {
                // 充值成功
                okLocalRechargeDetailRecords.add(item);
            } else if ("0".equalsIgnoreCase(state)) {
                // 待确认流水
                throw new Exception("对账: 存在待确认充值记录");
            } else {
                // 充值失败
                errorLocalRechargeDetailRecords.add(item);
            }
        }

        Iterator<NewEve> okThirdIterator = okThirdRechargeRecords.iterator();
        while (okThirdIterator.hasNext()) {
            Iterator<RechargeDetailLog> okLocalIterator = okLocalRechargeDetailRecords.iterator();
            // 即信流水对象
            NewEve newEve = okThirdIterator.next();
            while (okLocalIterator.hasNext()) {
                // 本地流水对象
                RechargeDetailLog rechargeDetailLog = okLocalIterator.next();
                long eveMoney = MoneyHelper.yuanToFen(newEve.getAmount());
                log.info("即信金额[" + eveMoney + "] 本地金额[" + rechargeDetailLog.getMoney() + "]");
                if (eveMoney == rechargeDetailLog.getMoney()) {
                    log.info(String.format("[充值对账] 核对成功 金额: %s", eveMoney));
                    okLocalIterator.remove();
                    okThirdIterator.remove();
                    break;
                }
            }
        }

        // 针对即信流水剩余, 进行补单
        if (!CollectionUtils.isEmpty(okThirdRechargeRecords)) {
            log.warn("[对账] 执行充值补单操作");
            okThirdIterator = okThirdRechargeRecords.iterator();
            // 充值失败的迭代器
            while (okThirdIterator.hasNext()) {
                // 即信流水
                Iterator<RechargeDetailLog> errorLocalIterator = errorLocalRechargeDetailRecords.iterator();
                NewEve newEve = okThirdIterator.next();
                while (errorLocalIterator.hasNext()) {
                    // 本地流水
                    RechargeDetailLog rechargeDetailLog = errorLocalIterator.next();
                    long eveMoney = MoneyHelper.yuanToFen(newEve.getAmount());
                    if (eveMoney == rechargeDetailLog.getMoney()) {
                        log.info(String.format("[充值对账] 找回充值记录 金额: %s", eveMoney));
                        boolean updateState = updateRechargeRecord(rechargeDetailLog);
                        String msg = GSON.toJson(rechargeDetailLog);
                        exceptionEmailHelper.sendErrorMessage(
                                String.format("[对账] 找回充值记录 %s", updateState ? "成功" : "失败"),
                                msg);

                        errorLocalIterator.remove();
                        okThirdIterator.remove();
                        break;
                    }

                }
            }

            if (!CollectionUtils.isEmpty(okThirdRechargeRecords)) {
                for (NewEve newEve : okThirdRechargeRecords) {
                    boolean insertState = insertRechargeRecord(newEve, userThirdAccount);
                    String msg = GSON.toJson(newEve);
                    exceptionEmailHelper.sendErrorMessage(
                            String.format("[对账] 补单充值记录 %s", insertState ? "成功" : "失败"),
                            msg);
                }
            }
        }

        // 针对本地充值流水剩余, 进行拨正
        if (!CollectionUtils.isEmpty(okLocalRechargeDetailRecords)) {
            log.warn("[对账] 执行充值拨正操作");
            for (RechargeDetailLog item : okLocalRechargeDetailRecords) {
                boolean cancelState = cancelRecord(item);
                String msg = GSON.toJson(item);
                exceptionEmailHelper.sendErrorMessage(
                        String.format("[对账] 取消充值记录 %s", cancelState ? "成功" : "失败"),
                        msg);
            }
        }

        return true;
    }


    /**
     * 充值记录补单
     *
     * @param newEve
     * @param userThirdAccount
     * @return
     */
    private boolean insertRechargeRecord(NewEve newEve, UserThirdAccount userThirdAccount) throws Exception {
        log.info("[离线充值对账, 新增充值记录] 开始");
        String transtype = newEve.getTranstype();
        AssetChangeTypeEnum assetChangeTypeEnum = null;
        try {
            assetChangeTypeEnum = AssetChangeTypeEnum.findByRemoteType(transtype);
        } catch (Exception e) {
            log.error("根据即信资金变动类型查询本地变动记录", e);
        }

        // 插入充值记录
        Date nowDate = new Date();
        String cendt = newEve.getCendt();
        String queryTime = newEve.getQueryTime();
        String year = queryTime.substring(0, 4);
        String dateStr = String.format("%s%s", year, cendt);
        Date createDate = DateHelper.stringToDate(dateStr, DateHelper.DATE_FORMAT_YMDHMS_NUM);
        RechargeDetailLog rechargeDetailLog = new RechargeDetailLog();
        rechargeDetailLog.setRechargeType(AssetChangeTypeEnum.offlineRecharge.equals(assetChangeTypeEnum) ? 1 : 0);  //充值类型
        rechargeDetailLog.setUserId(userThirdAccount.getUserId());
        rechargeDetailLog.setBankName(userThirdAccount.getBankName());
        rechargeDetailLog.setCallbackTime(nowDate);
        rechargeDetailLog.setCreateTime(createDate);
        rechargeDetailLog.setUpdateTime(nowDate);
        rechargeDetailLog.setCardNo(userThirdAccount.getCardNo());
        rechargeDetailLog.setDel(0);
        rechargeDetailLog.setIp("127.0.0.1");
        rechargeDetailLog.setMobile(userThirdAccount.getMobile());
        rechargeDetailLog.setMoney(MoneyHelper.yuanToFen(newEve.getAmount()));
        rechargeDetailLog.setRechargeChannel(0);
        rechargeDetailLog.setState(1);
        rechargeDetailLog.setSeqNo(newEve.getOrderno());
        rechargeDetailLog.setResponseMessage("正常补单, 即信账单Id" + newEve.getId());  // 响应吗
        rechargeDetailLogService.save(rechargeDetailLog);

        // 充值成功
        AssetChange entity = new AssetChange();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        String seqNo = rechargeDetailLog.getSeqNo();
        entity.setGroupSeqNo(groupSeqNo);
        entity.setMoney(rechargeDetailLog.getMoney());
        entity.setSeqNo(seqNo);
        entity.setUserId(userThirdAccount.getUserId());
        entity.setRemark(String.format("你在 %s 成功充值%s元", DateHelper.dateToString(nowDate), newEve.getAmount()));
        entity.setType(assetChangeTypeEnum);  // 充值类型
        assetChangeProvider.commonAssetChange(entity);
        log.info("[离线充值对账, 新增充值记录] 结束");
        return true;
    }


    /**
     * 将充值失败修改问充值成功, 并且执行充值资金变动
     *
     * @param rechargeDetailLog
     * @return
     */
    private boolean updateRechargeRecord(RechargeDetailLog rechargeDetailLog) throws Exception {
        log.info("[ 更改充值状态为成功]  开始");
        Date nowDate = new Date();
        rechargeDetailLog.setState(1);
        rechargeDetailLog.setCallbackTime(nowDate);
        rechargeDetailLogService.save(rechargeDetailLog);
        AssetChange entity = new AssetChange();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        entity.setGroupSeqNo(groupSeqNo);
        entity.setMoney(rechargeDetailLog.getMoney());
        entity.setSeqNo(rechargeDetailLog.getSeqNo());
        entity.setUserId(rechargeDetailLog.getUserId());
        entity.setRemark(
                String.format("你在 %s 成功充值%s元",
                        DateHelper.dateToString(nowDate),
                        MoneyHelper.divide(rechargeDetailLog.getMoney(), 100, 2)));

        entity.setSourceId(rechargeDetailLog.getId());
        if (rechargeDetailLog.getRechargeType() == 0) {
            entity.setType(AssetChangeTypeEnum.onlineRecharge);
        } else {
            entity.setType(AssetChangeTypeEnum.offlineRecharge);
        }
        assetChangeProvider.commonAssetChange(entity);

        // 触发用户充值
        MqConfig mqConfig = new MqConfig();
        mqConfig.setTag(MqTagEnum.RECHARGE);
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE);
        mqConfig.setSendTime(DateHelper.addSeconds(nowDate, 10));
        ImmutableMap<String, String> body = ImmutableMap.of(MqConfig.MSG_ID, rechargeDetailLog.getId().toString());
        mqConfig.setMsg(body);
        mqHelper.convertAndSend(mqConfig);
        log.info("[ 更改充值状态为成功]  结束");
        return true;
    }


    /**
     * 执行本地充值记录回撤
     *
     * @param rechargeDetailLog
     * @return
     */
    private boolean cancelRecord(RechargeDetailLog rechargeDetailLog) throws Exception {
        Date nowDate = new Date();
        log.info("[本地充值记录回撤] 开始");

        // 删除充值记录
        rechargeDetailLog.setState(2);
        rechargeDetailLog.setUpdateTime(nowDate);
        rechargeDetailLogService.save(rechargeDetailLog);

        // 资金撤回
        AssetChange entity = new AssetChange();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        entity.setGroupSeqNo(groupSeqNo);
        entity.setMoney(rechargeDetailLog.getMoney());
        entity.setSeqNo(rechargeDetailLog.getSeqNo());
        entity.setUserId(rechargeDetailLog.getUserId());
        entity.setRemark(
                String.format("系统在 %s 成功拨正充值资金%s元",
                        DateHelper.dateToString(nowDate),
                        MoneyHelper.divide(rechargeDetailLog.getMoney(), 100, 2)));
        entity.setSourceId(rechargeDetailLog.getId());
        entity.setType(AssetChangeTypeEnum.cancelRecharge);
        assetChangeProvider.commonAssetChange(entity);
        log.info("[本地充值记录回撤] 结束");
        return false;
    }


    /**
     * 查询本地充值流水
     *
     * @param userThirdAccount 用户类型
     * @param date             查询时间
     * @param rechargeType     充值类型
     * @return
     */
    private List<RechargeDetailLog> findLocalRechargeRecord(UserThirdAccount userThirdAccount,
                                                            Date date,
                                                            RechargeType rechargeType) throws Exception {
        Date beginDate = DateHelper.endOfDate(DateHelper.subDays(date, 1)); //  查询开始时间
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(date, 1)); // 查询结束时间
        String assetChangeType = "0";
        switch (rechargeType) {
            case offlineRecharge:
                assetChangeType = "1";
                break;
            case onlineRecharge:
                assetChangeType = "0";
                break;
            default:
                throw new Exception("错误的交易类型");
        }

        // 条件指定用户
        // 未删除
        // 符合时间跨度
        // 充值类型
        Specification<RechargeDetailLog> rechargeDetailLogSpecification = Specifications
                .<RechargeDetailLog>and()
                .eq("userId", userThirdAccount.getUserId())
                .eq("del", 0)
                .eq("rechargeType", assetChangeType)
                .between("createTime", new Range(beginDate, endDate))
                .build();

        List<RechargeDetailLog> rechargeDetailLogs = rechargeDetailLogService.findAll(rechargeDetailLogSpecification);
        Optional<List<RechargeDetailLog>> optinal = Optional.ofNullable(rechargeDetailLogs);
        return optinal.orElse(Lists.newArrayList());
    }


    public enum RechargeType {
        /**
         * 线下充值
         */
        offlineRecharge("7820"),
        /**
         * 在线充值
         */
        onlineRecharge("7822");

        RechargeType(String localType) {
            this.localType = localType;
        }

        private String localType;

        public String getLocalType() {
            return localType;
        }

        public void setLocalType(String localType) {
            this.localType = localType;
        }
    }

    private final static Gson GSON = new Gson();


}
