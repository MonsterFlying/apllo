package com.gofobao.framework.as.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.as.biz.CashStatementBiz;
import com.gofobao.framework.asset.entity.CashDetailLog;
import com.gofobao.framework.asset.service.CashDetailLogService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.financial.entity.NewAleve;
import com.gofobao.framework.financial.entity.NewEve;
import com.gofobao.framework.financial.service.NewAleveService;
import com.gofobao.framework.financial.service.NewEveService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.MoneyHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class CashStatementBizImpl implements CashStatementBiz {
    @Autowired
    private UserService userService;

    @Autowired
    private CashDetailLogService cashDetailLogService;

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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean offlineStatement(Long userId, Date date, CashType cashType) throws Exception {
        // 查询即信流水
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "userthirdAccount record is null");
        Preconditions.checkNotNull(date, "date is null");

        // 投资标的
        List<NewEve> thirdCashRecordList = null;
        try {
            thirdCashRecordList = newEveService.findAllByTranTypeAndDateAndAccountId(cashType.getType(), userThirdAccount.getAccountId(), date);
        } catch (Exception e) {
            log.error("[离线提现对账] 查询存管交易流水异常", e);
            return false;
        }

        // 本地提现记录
        List<CashDetailLog> localCashDetailLogs = findLocalCashRecords(userThirdAccount, date, cashType);
        if (CollectionUtils.isEmpty(localCashDetailLogs)) {
            log.warn("[离线提现对账] 查询本地培训记录为空");
        }

        if (CollectionUtils.isEmpty(thirdCashRecordList) &&
                CollectionUtils.isEmpty(localCashDetailLogs)) {
            log.warn("[离线提现对账] 本地提现记录与存管交易记录持平");
            return true;
        }

        // 匹配提现记录
        return this.doCashOfOfflineMatch(userThirdAccount, date, cashType, thirdCashRecordList, localCashDetailLogs);
    }

    /**
     * 离线提现记录匹配
     *
     * @param userThirdAccount
     * @param date
     * @param cashType
     * @param thirdCashRecordList
     * @param localCashDetailLogs
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doCashOfOfflineMatch(UserThirdAccount userThirdAccount,
                                        Date date,
                                        CashType cashType,
                                        List<NewEve> thirdCashRecordList,
                                        List<CashDetailLog> localCashDetailLogs) throws Exception {
        // 筛选出(拨正)即信交易
        // 拨正数据集合
        List<NewEve> errorThirdRecordList = new ArrayList<>();
        // 正确数据集合
        List<NewEve> okThirdRecordList = new ArrayList<>();
        for (NewEve item : thirdCashRecordList) {
            if ("1".equalsIgnoreCase(item.getErvind())) {
                errorThirdRecordList.add(item);
            } else {
                okThirdRecordList.add(item);
            }
        }

        // 本地提现记录筛选
        // 提现失败的数据
        List<CashDetailLog> errorLocalRecordList = new ArrayList<>();
        // 提现成功的数据
        List<CashDetailLog> okLocalRecordList = new ArrayList<>();
        for (CashDetailLog item : localCashDetailLogs) {
            Integer state = item.getState();
            if (3 == state) {
                // 成功
                okLocalRecordList.add(item);
            } else if (4 == state) {
                // 失败
                errorLocalRecordList.add(item);
            } else {
                log.warn(String.format("[离线提现对账] 提现记录状态不明确, %s", gson.toJson(item)));
                throw new Exception("当前提现记录状态不明确");
            }
        }

        // 匹配正确提现记录
        Iterator<NewEve> okThirdIterator = okThirdRecordList.iterator();
        while (okThirdIterator.hasNext()) {
            Iterator<CashDetailLog> okLocalIterator = okLocalRecordList.iterator();
            NewEve newEve = okThirdIterator.next();
            while (okLocalIterator.hasNext()) {
                // 本地流水对象
                CashDetailLog cashDetailLog = okLocalIterator.next();
                // 提现手续费
                long cashFee = findCashFeeByEve(newEve);
                long eveMoney = MoneyHelper.yuanToFen(newEve.getAmount());
                log.info("存管提现金额[" + eveMoney + "] 存管提现手续费[" + cashFee + "] 本地提现金额[" +
                        (cashDetailLog.getMoney() - cashDetailLog.getFee()) + "] 提现手续费 [" + cashDetailLog.getFee() + "]");
                if ((eveMoney == (cashDetailLog.getMoney() - cashDetailLog.getFee()))
                        && (cashFee == cashDetailLog.getFee())) {
                    log.info(String.format("[离线提现对账] 核对成功 金额: %s", eveMoney));
                    okLocalIterator.remove();
                    okThirdIterator.remove();
                    break;
                }
            }
        }

        // 当前还存在提现记录
        if (!CollectionUtils.isEmpty(okThirdRecordList)) {
            log.warn("[离线提现对账] 执行提现补单操作");
            okThirdIterator = okThirdRecordList.iterator();
            while (okThirdIterator.hasNext()) {
                // 即信流水
                Iterator<CashDetailLog> errorLocalIterator = errorLocalRecordList.iterator();
                NewEve newEve = okThirdIterator.next();
                // 提现手续费
                long cashFee = findCashFeeByEve(newEve);
                while (errorLocalIterator.hasNext()) {
                    // 本地流水
                    CashDetailLog cashDetailLog = errorLocalIterator.next();
                    long eveMoney = MoneyHelper.yuanToFen(newEve.getAmount());
                    if ((eveMoney == (cashDetailLog.getMoney() - cashDetailLog.getFee()))
                            && (cashFee == cashDetailLog.getFee())) {
                        log.info(String.format("[离线提现对账] 找回提现金额: %s", eveMoney));
                        boolean updateState = updateCashRecord(cashDetailLog);
                        String msg = gson.toJson(cashDetailLog);
                        exceptionEmailHelper.sendErrorMessage(
                                String.format("[离线提现对账] 找回提现记录 %s", updateState ? "成功" : "失败"),
                                msg);
                        errorLocalIterator.remove();
                        okThirdIterator.remove();
                        break;
                    }

                }
            }
            if (!CollectionUtils.isEmpty(okThirdRecordList)) {
                for (NewEve newEve : okThirdRecordList) {
                    boolean insertState = insertCashRecord(newEve, userThirdAccount);
                    String msg = gson.toJson(newEve);
                    exceptionEmailHelper.sendErrorMessage(
                            String.format("[离线提现对账] 补单充值记录 %s", insertState ? "成功" : "失败"),
                            msg);
                }
            }
        }

        if (!CollectionUtils.isEmpty(okLocalRecordList)) {
            log.warn("[离线提现对账] 拨正订单");
            for (CashDetailLog item : okLocalRecordList) {
                boolean cancelState = cancelRecord(item);
                String msg = gson.toJson(item);
                exceptionEmailHelper.sendErrorMessage(
                        String.format("[对账] 取消充值记录 %s", cancelState ? "成功" : "失败"),
                        msg);
            }
        }

        return true;
    }

    private boolean cancelRecord(CashDetailLog cashDetailLog) throws Exception {
        log.warn("[离线对账] 拨正提现记录 开始");
        log.warn(String.format("操作数据 %s", gson.toJson(cashDetailLog)));
        Date nowDate = new Date();
        // 更改提现记录为已取消
        cashDetailLog.setState(4);
        cashDetailLog.setCallbackTime(nowDate);
        cashDetailLogService.save(cashDetailLog);
        long userId = cashDetailLog.getUserId();

        // 更改用户资金
        AssetChange entity = new AssetChange();
        long realCashMoney = cashDetailLog.getMoney() - cashDetailLog.getFee();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        entity.setGroupSeqNo(groupSeqNo);
        entity.setMoney(realCashMoney);
        entity.setSeqNo(cashDetailLog.getSeqNo());
        entity.setUserId(userId);
        entity.setRemark(String.format("你在 %s 成功返还提现%s元", DateHelper.dateToString(nowDate),
                StringHelper.formatDouble(realCashMoney / 100D, true)));
        entity.setType(AssetChangeTypeEnum.cancelCash);
        assetChangeProvider.commonAssetChange(entity);
        if (cashDetailLog.getFee() > 0) {
            Long feeAccountId = assetChangeProvider.getFeeAccountId();
            entity = new AssetChange();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(cashDetailLog.getFee());
            entity.setSeqNo(cashDetailLog.getSeqNo());
            entity.setUserId(userId);
            entity.setForUserId(feeAccountId);
            entity.setSourceId(cashDetailLog.getId());
            entity.setRemark(String.format("你在 %s 成功返还提现手续费%s元", DateHelper.dateToString(nowDate),
                    StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true)));
            entity.setType(AssetChangeTypeEnum.cancelCashFee);
            assetChangeProvider.commonAssetChange(entity);

            // 平台收取提现手续费
            entity = new AssetChange();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(cashDetailLog.getFee());
            entity.setSeqNo(cashDetailLog.getSeqNo());
            entity.setUserId(feeAccountId);
            entity.setForUserId(userId);
            entity.setSourceId(cashDetailLog.getId());
            entity.setRemark(String.format("你在 %s 成功返还提现手续费%s元", DateHelper.dateToString(nowDate),
                    StringHelper.formatDouble(cashDetailLog.getFee() / 100D, true)));
            entity.setType(AssetChangeTypeEnum.cancelPlatformCashFee);
            assetChangeProvider.commonAssetChange(entity);
        }
        log.warn("[离线对账] 拨正提现记录 结束");
        return false;
    }

    /**
     * 创建提现记录
     *
     * @param newEve
     * @param userThirdAccount
     * @return
     */
    private boolean insertCashRecord(NewEve newEve, UserThirdAccount userThirdAccount) throws Exception {
        // 插入提现记录
        Date nowDate = new Date();
        String cendt = newEve.getCendt();
        String queryTime = newEve.getQueryTime();
        String year = queryTime.substring(0, 4);
        String dateStr = String.format("%s%s", year, cendt);
        Date createDate = DateHelper.stringToDate(dateStr, DateHelper.DATE_FORMAT_YMDHMS_NUM);
        String transtype = newEve.getTranstype(); // 提现类型
        boolean bigCashState = false;
        if (CashType.smallCash.getType().equalsIgnoreCase(transtype)) {
            bigCashState = false;
        } else {
            bigCashState = true;
        }
        long fee = findCashFeeByEve(newEve);
        // 查询流水是否存在提现手续费
        CashDetailLog cashDetailLog = new CashDetailLog();
        cashDetailLog.setThirdAccountId(userThirdAccount.getAccountId());
        cashDetailLog.setBankName(userThirdAccount.getBankName());
        cashDetailLog.setCardNo(userThirdAccount.getCardNo());
        cashDetailLog.setCashType(bigCashState ? 1 : 0);
        cashDetailLog.setCallbackTime(nowDate);
        /*if (bigCashState) {
            cashDetailLog.setCompanyBankNo(""); // 联行卡号
        }*/
        cashDetailLog.setFee(fee);
        cashDetailLog.setCreateTime(createDate);
        cashDetailLog.setMoney(MoneyHelper.yuanToFen(newEve.getAmount()));
        cashDetailLog.setSeqNo(newEve.getOrderno());
        // 充值成功
        cashDetailLog.setState(3);
        cashDetailLog.setVerifyTime(nowDate);
        cashDetailLog.setVerifyUserId(0L);
        cashDetailLog.setUserId(userThirdAccount.getUserId());
        cashDetailLog.setVerifyRemark("系统补单通过");
        cashDetailLog = cashDetailLogService.save(cashDetailLog);

        AssetChange entity = new AssetChange();
        long realCashMoney = cashDetailLog.getMoney() - cashDetailLog.getFee();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        entity.setGroupSeqNo(groupSeqNo);
        entity.setMoney(realCashMoney);
        entity.setSeqNo(cashDetailLog.getSeqNo());
        entity.setUserId(cashDetailLog.getUserId());
        entity.setRemark(String.format("你在 %s 成功提现%s元", DateHelper.dateToString(nowDate),
                MoneyHelper.divide(realCashMoney, 100, 2)));
        if (cashDetailLog.getCashType() == 0) { // 小额提现
            entity.setType(AssetChangeTypeEnum.smallCash);
        } else {
            entity.setType(AssetChangeTypeEnum.bigCash);
        }

        assetChangeProvider.commonAssetChange(entity);
        if (cashDetailLog.getFee() > 0) {
            // 扣除用户手续费
            Long feeAccountId = assetChangeProvider.getFeeAccountId();
            entity = new AssetChange();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(cashDetailLog.getFee());
            entity.setSeqNo(cashDetailLog.getSeqNo());
            entity.setUserId(cashDetailLog.getUserId());
            entity.setForUserId(feeAccountId);
            entity.setRemark(String.format("你在%s成功扣除提现手续费%s元", DateHelper.dateToString(nowDate),
                    MoneyHelper.divide(cashDetailLog.getFee(), 100, 2)));
            if (cashDetailLog.getCashType() == 0) {
                // 小额提现
                entity.setType(AssetChangeTypeEnum.smallCashFee);
            } else {
                // 大额提现
                entity.setType(AssetChangeTypeEnum.bigCashFee);
            }
            assetChangeProvider.commonAssetChange(entity);

            // 平台添加手续费
            entity = new AssetChange();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(cashDetailLog.getFee());
            entity.setSeqNo(cashDetailLog.getSeqNo());
            entity.setUserId(feeAccountId);
            entity.setForUserId(cashDetailLog.getUserId());
            entity.setRemark(String.format("你在%s成功收取提现手续费%s元", DateHelper.dateToString(nowDate),
                    MoneyHelper.divide(cashDetailLog.getFee(), 100, 2)));
            if (cashDetailLog.getCashType() == 0) {
                // 小额提现
                entity.setType(AssetChangeTypeEnum.platformSmallCashFee);
            } else {
                // 大额提现
                entity.setType(AssetChangeTypeEnum.platformBigCashFee);
            }
            assetChangeProvider.commonAssetChange(entity);
        }

        return true;
    }

    /**
     * 查找提现是否存在手续费
     *
     * @param newEve
     * @return
     * @throws Exception
     */
    public long findCashFeeByEve(NewEve newEve) throws Exception {
        // 系统流水跟踪号
        String seqno = newEve.getSeqno();
        // 交易类型
        String transtype = newEve.getTranstype();
        // 电子账户
        String cardnbr = newEve.getCardnbr();
        // 入库时间
        String queryTime = newEve.getQueryTime();
        Specification<NewAleve> newAleveSpecification = Specifications.<NewAleve>and()
                .eq("cardnbr", cardnbr)
                .eq("transtype", transtype)
                .eq("seqno", seqno)
                .eq("queryTime", queryTime)
                .build();
        List<NewAleve> newAleves = newAleveService.findAll(newAleveSpecification);
        if (CollectionUtils.isEmpty(newAleves)) {
            throw new Exception("查询当前Aleve 记录为空");
        }
        if (newAleves.size() > 1) {
            throw new Exception("查询当前aleve 记录数大于1");
        }
        NewAleve newAleve = newAleves.get(0);

        Specification<NewAleve> oriSpecification = Specifications
                .<NewAleve>and()
                .eq("oriTranno", newAleve.getTranno())
                .eq("queryTime", queryTime)
                .eq("cardnbr", cardnbr)
                .build();
        List<NewAleve> feeNewAleve = newAleveService.findAll(oriSpecification);
        if (CollectionUtils.isEmpty(feeNewAleve)) {
            return 0;
        } else {
            if (feeNewAleve.size() > 1) {
                throw new Exception("查询当前aleve 记录数大于1");
            } else {
                String amount = feeNewAleve.get(0).getAmount();
                return MoneyHelper.yuanToFen(amount);
            }
        }
    }


    /**
     * 更改提现记录
     *
     * @param cashDetailLog
     * @return
     */
    private boolean updateCashRecord(CashDetailLog cashDetailLog) throws Exception {
        log.info("[离线提现对账] 更改提现状态为成功开始");
        Date nowDate = new Date();
        // 充值成功
        cashDetailLog.setState(3);
        cashDetailLog.setCallbackTime(nowDate);
        cashDetailLog.setVerifyRemark("系统对账补单(将提现失败修改成提现成功)");
        cashDetailLogService.save(cashDetailLog);

        // 资金变动
        AssetChange entity = new AssetChange();
        long realCashMoney = cashDetailLog.getMoney() - cashDetailLog.getFee();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        entity.setGroupSeqNo(groupSeqNo);
        entity.setMoney(realCashMoney);
        entity.setSeqNo(cashDetailLog.getSeqNo());
        entity.setUserId(cashDetailLog.getUserId());
        entity.setRemark(String.format("你在 %s 成功提现%s元", DateHelper.dateToString(nowDate),
                MoneyHelper.divide(realCashMoney, 100, 2)));
        if (cashDetailLog.getCashType() == 0) { // 小额提现
            entity.setType(AssetChangeTypeEnum.smallCash);
        } else {
            entity.setType(AssetChangeTypeEnum.bigCash);
        }

        assetChangeProvider.commonAssetChange(entity);
        if (cashDetailLog.getFee() > 0) {
            Long feeAccountId = assetChangeProvider.getFeeAccountId();
            entity = new AssetChange();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(cashDetailLog.getFee());
            entity.setSeqNo(cashDetailLog.getSeqNo());
            entity.setUserId(cashDetailLog.getUserId());
            entity.setForUserId(feeAccountId);
            entity.setRemark(String.format("你在 %s 成功扣除提现手续费%s元", DateHelper.dateToString(nowDate),
                    MoneyHelper.divide(cashDetailLog.getFee(), 100, 2)));
            if (cashDetailLog.getCashType() == 0) { // 小额提现
                entity.setType(AssetChangeTypeEnum.smallCashFee);
            } else {
                entity.setType(AssetChangeTypeEnum.bigCashFee);
            }
            assetChangeProvider.commonAssetChange(entity);

            // 平台收取提现手续费
            entity = new AssetChange();
            entity.setGroupSeqNo(groupSeqNo);
            entity.setMoney(cashDetailLog.getFee());
            entity.setSeqNo(cashDetailLog.getSeqNo());
            entity.setUserId(feeAccountId);
            entity.setForUserId(cashDetailLog.getUserId());
            entity.setRemark(String.format("你在 %s 成功收取提现手续费%s元", DateHelper.dateToString(nowDate),
                    MoneyHelper.divide(cashDetailLog.getFee(), 100, 2)));
            if (cashDetailLog.getCashType() == 0) {
                entity.setType(AssetChangeTypeEnum.platformSmallCashFee);
            } else {
                entity.setType(AssetChangeTypeEnum.platformBigCashFee);
            }
            assetChangeProvider.commonAssetChange(entity);
        }
        log.info("[离线提现对账] 更改提现状态为成功结束");
        return true;
    }

    /**
     * 查询本地提现记录
     *
     * @param userThirdAccount
     * @param date
     * @param cashType
     * @return
     */
    private List<CashDetailLog> findLocalCashRecords(UserThirdAccount userThirdAccount, Date date, CashType cashType) throws Exception {
        Date beginDate = DateHelper.endOfDate(DateHelper.subDays(date, 1)); //  查询开始时间
        Date endDate = DateHelper.beginOfDate(DateHelper.addDays(date, 1)); // 查询结束时间
        String cashTypeStr = "0";
        switch (cashType) {
            // 大额提现
            case bigCash:
                cashTypeStr = "1";
                break;
            // 小额提现
            case smallCash:
                cashTypeStr = "0";
                break;
            default:
                throw new Exception("错误提现类型");
        }

        // 条件指定用户
        // 未删除
        // 符合时间跨度
        // 充值类型
        Specification<CashDetailLog> cashDetailLogSpecification = Specifications
                .<CashDetailLog>and()
                .eq("userId", userThirdAccount.getUserId())
                .eq("cashType", cashTypeStr)
                .between("createTime", new Range(beginDate, endDate))
                .build();

        return cashDetailLogService.findAll(cashDetailLogSpecification);
    }

    @Override
    public boolean onlineStatement(Long userId, Date date, CashType cashType, boolean force) throws Exception {
        return false;
    }

    public enum CashType {
        /**
         * 小额提现 2616
         */
        smallCash("2820"),

        /**
         * 大额提现
         */
        bigCash("2820");

        CashType(String type) {
            this.type = type;
        }

        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private final static Gson gson = new Gson();
}
