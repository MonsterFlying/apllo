package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeReq;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeResp;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestReq;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestResp;
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvest;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.common.data.LeSpecification;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.contract.biz.ContractBiz;
import com.gofobao.framework.contract.service.ContractService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoBuyTransfer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/8/1.
 */
@Component
@Slf4j
public class TransferProvider {

    @Autowired
    private TransferService transferService;
    @Autowired
    private AutoTenderService autoTenderService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private TransferBiz transferBiz;
    @Autowired
    private TransferBuyLogService transferBuyLogService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private BatchAssetChangeService batchAssetChangeService;
    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;

    @Autowired
    private AssetChangeProvider assetChangeProvider;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    @Autowired
    private ContractService contractService;

    @Autowired
    private JixinManager jixinManager;

    final Gson GSON = new GsonBuilder().create();

    /**
     * 自动债权转让
     *
     * @param msg
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void autoTransfer(Map<String, String> msg) throws Exception {
        /*
         * 1.批次自动投标规则
         * 2.筛选合适的自动投标规则进行购买债权
         * 3.更新自动投标规则
         */
        Date nowDate = new Date();
        long transferId = NumberHelper.toLong(msg.get(MqConfig.MSG_TRANSFER_ID));/* 债权转让id */
        Transfer transfer = transferService.findByIdLock(transferId);/* 债权转让记录 */
        Preconditions.checkNotNull(transfer, "债权转让记录不存在!");
        Borrow borrow = borrowService.findById(transfer.getBorrowId());/* 债权转让 借款记录 */
        Preconditions.checkNotNull(borrow, "借款记录不存在!");
        String[] RepayFashions = BorrowHelper.countRepayFashions(borrow.getRepayFashion()).split(",");/* 还款方式 */

        int maxSize = 50;
        int pageNum = 0;
        List<AutoTender> autoTenderList = null;
        // 0、不限定，1、按月，2、按天
        int notTimeLimitType = borrow.getRepayFashion() == 1 ? 1 : 2;/* 如果债权转让 借款是按月分期  则notTimelimitType为2,借款是按天 则notTimelimitType为1*/
        List<Long> userIds = null;/* 匹配上的自动投标规则 */
        Specification<Asset> as = null;/* 匹配资产规则 */
        Specification<UserThirdAccount> utas = null;/* 匹配存管账户记录规则 */
        long transferMoneyYes = transfer.getTransferMoneyYes();/* 债权转让已购金额 */
        long transferMoney = transfer.getTransferMoney();/* 债权转让金额 */
        List<Long> tenderUserIds = new ArrayList<>();/* 已经购买债权的用户id */
        List<Long> autoTenderIds = new ArrayList<>();/* 已经触发的自动投标id */

        //变量定义
        Map<Long, Asset> assetMaps = null;
        Asset asset = null;
        Map<Long, UserThirdAccount> userThirdAccountMaps = null;
        UserThirdAccount userThirdAccount = null;
        boolean flag = false;
        int autoTenderCount = 0;

        Specification<AutoTender> ats = Specifications/* 匹配自动投标规则 */
                .<AutoTender>and()
                .eq("tender3", 1)
                .eq("status", 1)
                .notIn("userId", transfer.getUserId())/* 排除转让人自动投标规则 */
                .in("repayFashions", RepayFashions)
                .notIn("timelimitType", notTimeLimitType)
                .predicate(new GeSpecification("timelimitLast", new DataObject(transfer.getTimeLimit())))
                .predicate(new LeSpecification("timelimitFirst", new DataObject(transfer.getTimeLimit())))
                .predicate(new GeSpecification("aprFirst", new DataObject(transfer.getApr())))
                .predicate(new LeSpecification("aprLast", new DataObject(transfer.getApr())))
                .build();

        do {
            autoTenderList = autoTenderService.findList(ats, new PageRequest(pageNum++, maxSize, new Sort(Sort.Direction.ASC, "order")));
            if (CollectionUtils.isEmpty(autoTenderList)) {
                log.info("自动购买债权转让MQ：第" + (pageNum + 1) + "页,没有匹配到自动投标规则！");
                break;
            }

            userIds = autoTenderList.stream().map(autoTender -> autoTender.getUserId()).collect(Collectors.toList());
            //查询自动投标投资人的资产记录
            as = Specifications
                    .<Asset>and()
                    .in("userId", userIds.toArray())
                    .build();
            assetMaps = assetService.findList(as).stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));
            //查询自动投标投资人的存管账户记录
            utas = Specifications
                    .<UserThirdAccount>and()
                    .in("userId", userIds.toArray())
                    .build();
            userThirdAccountMaps = userThirdAccountService.findList(utas).stream().collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));

            for (AutoTender autoTender : autoTenderList) {
                asset = assetMaps.get(autoTender.getUserId());/* 自动投标投资人资产记录 */
                userThirdAccount = userThirdAccountMaps.get(autoTender.getUserId());/* 自动投标投资人存管信息记录 */

                // 保证每个用户 和 每个自动投标规则只能使用一次
                if (tenderUserIds.contains(NumberHelper.toLong(autoTender.getUserId()))
                        || autoTenderIds.contains(NumberHelper.toLong(autoTender.getId()))) {
                    continue;
                }

                //判断自动投标投资人是否开户
                if (ThirdAccountHelper.allConditionCheck(userThirdAccount).getBody().getState().getCode() != VoBaseResp.OK) {
                    continue;
                }
                // 判断是否满标或者 达到自动投标最大额度
                if ((transferMoneyYes >= transferMoney)) {
                    flag = true;
                    break;
                }

                long useMoney = asset.getUseMoney();  // 用户可用金额
                long buyMoney = autoTender.getMode() == 1 ? autoTender.getTenderMoney() : useMoney;/* 有效购买金额 */
                buyMoney = Math.min(useMoney - autoTender.getSaveMoney(), buyMoney);
                long lowest = autoTender.getLowest(); // 最小投标金额
                if ((buyMoney < lowest)) {
                    continue;
                }

                // 标的金额小于 最小投标金额
                if (transferMoney - transferMoneyYes < lowest) {
                    continue;
                }

                if ((!tenderUserIds.contains(autoTender.getUserId()))
                        && (!autoTenderIds.contains(autoTender.getId()))) {  // 保证自动不能重复
                    //购买债权转让
                    VoBuyTransfer voBuyTransfer = new VoBuyTransfer();
                    voBuyTransfer.setUserId(autoTender.getUserId());
                    voBuyTransfer.setAuto(true);
                    voBuyTransfer.setBuyMoney(MoneyHelper.round(buyMoney / 100.0, 2));
                    voBuyTransfer.setAutoOrder(autoTender.getOrder());
                    ResponseEntity<VoBaseResp> response = transferBiz.buyTransfer(voBuyTransfer);
                    if (response.getStatusCode().equals(HttpStatus.OK)) { //购买债权转让成功后更新自动投标规则
                        transferMoneyYes += lowest;
                        autoTenderIds.add(autoTender.getId());
                        tenderUserIds.add(autoTender.getUserId());
                        autoTender.setAutoAt(nowDate);
                        autoTender.setUpdatedAt(nowDate);
                        autoTenderService.updateById(autoTender);
                        autoTenderCount++;
                    } else {
                        continue;
                    }
                }

            }
        } while (autoTenderList.size() >= maxSize && !flag);

        if (autoTenderCount >= 1) {//如果自动投标被触发则更新自动投标规则
            autoTenderService.updateAutoTenderOrder();
        }

        // 解除锁定
        if (transferMoneyYes != transferMoney) { // 在自动投标中, 标的未满.马上将其解除.
            transfer.setUpdatedAt(nowDate);
            transfer.setIsLock(false);
            transferService.save(transfer);
        }
    }

    /**
     * 债权转让复审
     *
     * @param msg
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean againVerifyTransfer(Map<String, String> msg) throws Exception {
        long transferId = NumberHelper.toLong(msg.get(MqConfig.MSG_TRANSFER_ID));/* 债权转让id */
        Transfer transfer = transferService.findByIdLock(transferId);
        Preconditions.checkNotNull(transfer, "债权转让记录不存在!");
        if (transfer.getState() != 1) {
            log.error("复审：债权转让状态已发生改变！transferId:" + transferId);
            return false;
        }

        Specification<TransferBuyLog> tbls = Specifications
                .<TransferBuyLog>and()
                .eq("transferId", transfer.getId())
                .eq("state", 0)
                .eq("del", 0)
                .build();
        List<TransferBuyLog> transferBuyLogList = transferBuyLogService.findList(tbls);/* 购买债权转让记录 */
        Preconditions.checkState(!CollectionUtils.isEmpty(transferBuyLogList), "批量债权转让：购买债权记录不存在!");
        Tender parentTender = tenderService.findById(transfer.getTenderId());/* 转让投资记录 */
        Preconditions.checkNotNull(parentTender, "批量债权转让: 债权原始投标信息为空!");
        UserThirdAccount transferUserThirdAccount = userThirdAccountService.findByUserId(transfer.getUserId());/* 债权转让人开户信息 */
        Preconditions.checkNotNull(transferUserThirdAccount, "债权转让人开户记录不存在!");
        Borrow parentBorrow = borrowService.findById(transfer.getBorrowId());
        Preconditions.checkNotNull(parentBorrow, "债权转让原借款记录不存在!");

        log.info(String.format("复审: 批量债权转让申请开始: %s", GSON.toJson(msg)));

        //登记存管债权转让
        ImmutableList<Object> result = registerThirdTransferTender(transfer, transferBuyLogList, parentTender, transferUserThirdAccount, parentBorrow);
        Iterator<Object> iterator = result.iterator();
        String batchNo = StringHelper.toString(iterator.next());
        /* 转让管理费 */
        long transferFee = NumberHelper.toLong(iterator.next());
        //增加批次资金变动记录
        addBatchAssetChangeByTransfer(transferId, transfer, transferBuyLogList, transferFee, batchNo);
        //增加successAt时间
        transfer.setSuccessAt(new Date());
        transferService.save(transfer);
           //TODO 债转合同生成成功
        /*    try {
            List<BorrowContract> borrowContracts = contractService.findByBorrowId(transfer.getBorrowId(), batchNo, false);
            if (!CollectionUtils.isEmpty(borrowContracts)) {
                contractService.updateContractStatus(transfer.getBorrowId(), batchNo);
            }
            log.info("标的放款成功，合同生成成功,打印合同用户的信息：" + GSON.toJson(borrowContracts));
        } catch (Exception e) {
            log.info("标的放款放款成功,合同失败", e);
        }*/
        log.info(String.format("复审: 批量债权转让申请成功: %s", GSON.toJson(msg)));
        return true;

    }

    /**
     * 增加批次资金变动记录
     *
     * @param transferId
     * @param transfer
     * @param transferBuyLogList
     * @param transferFee
     * @param batchNo
     */
    private void addBatchAssetChangeByTransfer(long transferId, Transfer transfer, List<TransferBuyLog> transferBuyLogList, long transferFee, String batchNo) throws ExecutionException {
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        Date nowDate = new Date();
        // 扣除债权购买人冻结资金
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setBatchNo(batchNo);
        batchAssetChange.setSourceId(transferId);
        batchAssetChange.setType(BatchAssetChangeContants.BATCH_CREDIT_INVEST);
        batchAssetChange.setState(0);
        batchAssetChange.setCreatedAt(nowDate);
        batchAssetChange.setUpdatedAt(nowDate);
        batchAssetChange = batchAssetChangeService.save(batchAssetChange);

        //债权转让总本金
        long transferPrincipal = transferBuyLogList.stream().filter(transferBuyLog -> transferBuyLog.getState() == 0).mapToLong(TransferBuyLog::getPrincipal).sum();

        long batchAssetChangeId = batchAssetChange.getId();
        // 债权转让人收款 = 转让本金加应收利息
        BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setType(AssetChangeTypeEnum.batchSellBonds.getLocalType());  // 出售债权
        batchAssetChangeItem.setUserId(transfer.getUserId());
        batchAssetChangeItem.setMoney(transferPrincipal + transfer.getAlreadyInterest());
        batchAssetChangeItem.setRemark(String.format("出售债权[%s]获得待收本金和应计利息%s元", transfer.getTitle(),
                StringHelper.formatDouble((transferPrincipal + transfer.getAlreadyInterest()), 100D, true)));
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItem.setSourceId(transferId);
        batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItemService.save(batchAssetChangeItem);
        Long feeAccountId = assetChangeProvider.getFeeAccountId();  // 平台ID

        // 扣除原始债权转让人转让费
        batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setType(AssetChangeTypeEnum.batchSellBondsFee.getLocalType());  // 扣除债权转让人手续费
        batchAssetChangeItem.setUserId(transfer.getUserId());
        batchAssetChangeItem.setForUserId(feeAccountId);
        batchAssetChangeItem.setMoney(transferFee);
        batchAssetChangeItem.setRemark(String.format("扣除出售债权[%s]手续费%s元", transfer.getTitle(),
                StringHelper.formatDouble(transferFee, 100D, true)));
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItem.setSourceId(transferId);
        batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItemService.save(batchAssetChangeItem);

        // 收费账户添加债权转让费用
        batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setType(AssetChangeTypeEnum.platformBatchSellBondsFee.getLocalType());  // 收取债权转让人手续费
        batchAssetChangeItem.setUserId(feeAccountId);  // 平台收费人
        batchAssetChangeItem.setForUserId(transfer.getUserId());
        batchAssetChangeItem.setMoney(transferFee);
        batchAssetChangeItem.setRemark(String.format("收取出售债权[%s]手续费%s元", transfer.getTitle(),
                StringHelper.formatDouble(transferFee, 100D, true)));
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItem.setSourceId(transferId);
        batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItemService.save(batchAssetChangeItem);

        for (TransferBuyLog transferBuyLog : transferBuyLogList) {
            // 扣除债权转让购买人冻结资金
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setType(AssetChangeTypeEnum.batchBuyClaims.getLocalType());
            batchAssetChangeItem.setUserId(transferBuyLog.getUserId());
            batchAssetChangeItem.setForUserId(transfer.getUserId());
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setMoney(transferBuyLog.getValidMoney());
            batchAssetChangeItem.setRemark(String.format("购买债权[%s], 成功扣除资金%s元", transfer.getTitle(),
                    StringHelper.formatDouble(transferBuyLog.getValidMoney(), 100D, true)));
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItem.setSourceId(transferBuyLog.getId());
            batchAssetChangeItemService.save(batchAssetChangeItem);
        }
    }


    @Autowired
    private ContractBiz contractBiz;

    /**
     * 登记存管债权转让
     *
     * @param transfer
     * @param transferBuyLogList
     * @param parentTender
     * @param transferUserThirdAccount
     * @param parentBorrow
     * @throws Exception
     */
    private ImmutableList<Object> registerThirdTransferTender(Transfer transfer, List<TransferBuyLog> transferBuyLogList, Tender parentTender, UserThirdAccount transferUserThirdAccount, Borrow parentBorrow) throws Exception {
        Date nowDate = new Date();
        List<CreditInvest> creditInvestList = new ArrayList<>();
        CreditInvest creditInvest = null;
        UserThirdAccount tenderUserThirdAccount = null;
        /**
         * 购买债权 买方需要付出多少钱
         */
        int sumAmount = 0;
        // 转让管理费
        int sumTransferFee = 0;
        /* 债权转让管理费费率 */
        double transferFeeRate = BorrowHelper.getTransferFeeRate(transfer.getTimeLimit());
        long transferFee = new Double(MoneyHelper.round(MoneyHelper.multiply(transfer.getPrincipal(), transferFeeRate), 0)).longValue();  /* 转让管理费 */
        /**
         * 本金之和
         */
        for (int i = 0; i < transferBuyLogList.size(); i++) {
            TransferBuyLog transferBuyLog = transferBuyLogList.get(i);
            /* 债权转让购买人存管账户信息 */
            tenderUserThirdAccount = userThirdAccountService.findByUserId(transferBuyLog.getUserId());
            Preconditions.checkNotNull(tenderUserThirdAccount, "投资人开户记录不存在!");
            //购买债权转让有效金额 本金
            long principal = new Double(MoneyHelper.round(transferBuyLog.getPrincipal(), 0)).longValue();
            //收取转让人债权转让管理费
            long tempTransferFee = new Double(MoneyHelper.round(MoneyHelper.multiply(transferBuyLog.getValidMoney() / new Double(transfer.getPrincipal()), transferFee), 0)).longValue();
            /**
             * 卖放转出债权给平台的费用，
             */
            long txFee = tempTransferFee;
            /**
             * 卖总的转让管理费
             */
            sumTransferFee += tempTransferFee;
            /**
             * 购买债权 买方需要付出多少钱  买入本金+当期应计利息
             */
            long txAmount = principal + transferBuyLog.getAlreadyInterest();
            //判断标的已在存管登记转让
            if (BooleanHelper.isTrue(transferBuyLog.getThirdTransferFlag())) {
                continue;
            }
            /* 购买债权转让orderId */
            String transferOrderId = JixinHelper.getOrderId(JixinHelper.LEND_REPAY_PREFIX);
            creditInvest = new CreditInvest();
            creditInvest.setAccountId(tenderUserThirdAccount.getAccountId());
            creditInvest.setOrderId(transferOrderId);
            creditInvest.setTxAmount(StringHelper.formatDouble(txAmount, 100, false));
            creditInvest.setTxFee(StringHelper.formatDouble(txFee, 100, false));
            creditInvest.setTsfAmount(StringHelper.formatDouble(principal, 100, false));
            creditInvest.setForAccountId(transferUserThirdAccount.getAccountId());
            creditInvest.setOrgOrderId(parentTender.getThirdTenderOrderId());
            creditInvest.setOrgTxAmount(StringHelper.formatDouble(parentTender.getValidMoney(), 100, false));
            creditInvest.setProductId(parentBorrow.getProductId());
            creditInvest.setContOrderId(tenderUserThirdAccount.getAutoTransferBondOrderId());
            creditInvestList.add(creditInvest);
            transferBuyLog.setThirdTransferOrderId(transferOrderId);

            //解除存管资金冻结
            String newOrderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);/* 购买债权转让冻结金额 orderid */
            UserThirdAccount buyUserThirdAccount = userThirdAccountService.findByUserId(transferBuyLog.getUserId());
            BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
            balanceUnfreezeReq.setAccountId(buyUserThirdAccount.getAccountId());
            balanceUnfreezeReq.setTxAmount(StringHelper.formatDouble(MoneyHelper.divide(transferBuyLog.getValidMoney(), 100d), false));
            balanceUnfreezeReq.setChannel(ChannelContant.HTML);
            balanceUnfreezeReq.setOrderId(newOrderId);
            balanceUnfreezeReq.setOrgOrderId(transferBuyLog.getFreezeOrderId());
            BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
            if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
                log.error("购买债权转让解冻资金失败：" + balanceUnfreezeResp.getRetMsg());
            }

            // 全部有效投保金额
            sumAmount += txAmount;
        }
        //更新债权转让购买记录
        transferBuyLogService.save(transferBuyLogList);

        //批次号
        String batchNo = JixinHelper.getBatchNo();
        //todo 绑定债转合同
       /* Borrow borrow = borrowService.findById(transfer.getBorrowId());
        try {
            Long transferUserId = transfer.getUserId();
            UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(transferUserId);
            if (!ObjectUtils.isEmpty(userThirdAccount)
                    && !StringUtils.isEmpty(userThirdAccount.getOpenAccountAt())
                    && userThirdAccount.getEntrustState()) {
                log.info("=============调用债权转让生成合同===============");
                BindBorrow bindBorrow = new BindBorrow();
                bindBorrow.setProductId(transfer.getBorrowId());
                bindBorrow.setTradeType(ContractContants.TRANSFER);
                //todo 合同模板没确定
                bindBorrow.setTemplateId(147);
                contractBiz.debtTemplate(bindBorrow);
                //记录
                List<BorrowContract> borrowContracts = Lists.newArrayList();
                transferBuyLogList.forEach(transferBuyLog -> {
                    Long userId = transferBuyLog.getUserId();
                    UserThirdAccount userThirdAccount1 = userThirdAccountService.findByUserId(userId);
                    if (userThirdAccount1.getEntrustState()) {
                        BorrowContract borrowContract = new BorrowContract();
                        borrowContract.setBatchNo(batchNo);
                        borrowContract.setForUserId(transferBuyLog.getUserId());
                        borrowContract.setType(2);
                        borrowContract.setCreatedAt(new Date());
                        borrowContract.setUserId(transfer.getUserId());
                        borrowContract.setBorrowId(transfer.getBorrowId());
                        borrowContract.setUpdateAt(new Date());
                        borrowContract.setBorrowName(borrow.getName());
                        borrowContract.setStatus(false);
                        borrowContracts.add(borrowContract);
                    }
                });
                if (!CollectionUtils.isEmpty(borrowContracts)) {
                    contractService.bitchSave(borrowContracts);
                }
            }
        } catch (Exception e) {
            log.error("调用债权转让生成合同失败", e);
        }*/
        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("transferId", transfer.getId());
        //调用存管批次债权转让接口
        BatchCreditInvestReq request = new BatchCreditInvestReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(sumAmount, 100, false));
        request.setTxCounts(StringHelper.toString(creditInvestList.size()));
        request.setSubPacks(GSON.toJson(creditInvestList));
        request.setAcqRes(GSON.toJson(acqResMap));
        request.setChannel(ChannelContant.HTML);
        request.setNotifyURL(javaDomain + "/pub/tender/v2/third/batch/creditinvest/check");
        request.setRetNotifyURL(javaDomain + "/pub/tender/v2/third/batch/creditinvest/run");
        BatchCreditInvestResp response = jixinManager.send(JixinTxCodeEnum.BATCH_CREDIT_INVEST, request, BatchCreditInvestResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            BatchCancelReq batchCancelReq = new BatchCancelReq();
            batchCancelReq.setBatchNo(batchNo);
            batchCancelReq.setTxAmount(StringHelper.formatDouble(sumAmount, 100, false));
            batchCancelReq.setTxCounts(StringHelper.toString(creditInvestList.size()));
            batchCancelReq.setChannel(ChannelContant.HTML);
            BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
            if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                throw new Exception("即信批次撤销失败!");
            }
            log.error(String.format("复审: 批量债权转让申请失败: %s", response));
            throw new Exception("投资人批次购买债权失败!:" + response.getRetMsg());
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setTxDate(request.getTxDate());
        thirdBatchLog.setTxTime(request.getTxTime());
        thirdBatchLog.setSeqNo(request.getSeqNo());
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(transfer.getId());
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_CREDIT_INVEST);
        thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
        thirdBatchLog.setRemark("投资人批次购买债权");
        thirdBatchLogService.save(thirdBatchLog);

        ImmutableList<Object> immutableList = ImmutableList.of(batchNo, sumTransferFee);
        return immutableList;
    }
}
