package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestReq;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestResp;
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvest;
import com.gofobao.framework.asset.contants.AssetTypeContants;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Zeke on 2017/8/14.
 */
@Slf4j
@Component
public class FinancePlanProvider {
    static Gson GSON = new GsonBuilder().create();

    @Value("${gofobao.adminDomain}")
    private String adminDomain;
    @Autowired
    private TransferService transferService;
    @Autowired
    private TransferBuyLogService transferBuyLogService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Value("${gofobao.javaDomain}")
    private String javaDomain;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;
    @Autowired
    private AssetChangeProvider assetChangeProvider;
    @Autowired
    private BatchAssetChangeService batchAssetChangeService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;

    /**
     * n
     * 理财计划满标后通知
     */
    public void pullScaleNotify(Map<String, String> msg) {
        try {
            Map<String, String> requestMaps = ImmutableMap.of("paramStr", GSON.toJson(msg), "sign", SecurityHelper.getSign(GSON.toJson(msg)));
            String resultStr = OKHttpHelper.postForm(adminDomain + "/api/open/finance-plan/review", requestMaps, null);
            System.out.print("返回响应:" + resultStr);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    /**
     * 理财计划债权转让复审
     *
     * @param msg
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean againVerifyFinanceTransfer(Map<String, String> msg) throws Exception {
        long transferId = NumberHelper.toLong(msg.get(MqConfig.MSG_TRANSFER_ID));/* 债权转让id */
        String isRepurchase = msg.get(MqConfig.IS_REPURCHASE);/*理财计划是否是赎回*/
        Transfer transfer = transferService.findByIdLock(transferId);
        Preconditions.checkNotNull(transfer, "理财计划债权转让记录不存在!");
        if (transfer.getState() != 1) {
            log.error("复审：理财计划债权转让状态已发生改变！transferId:" + transferId);
            return false;
        }

        // 2判断提交还款批次是否多次重复提交
        ThirdBatchLog thirdBatchLog = thirdBatchLogBiz.getValidLastBatchLog(String.valueOf(transferId), ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST);
        int flag = thirdBatchLogBiz.checkBatchOftenSubmit(String.valueOf(transferId),
                ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST);
        if (flag == ThirdBatchLogContants.AWAIT) {
            return false;
        } else if (flag == ThirdBatchLogContants.SUCCESS) {
            //墊付批次处理
            //触发处理批次放款处理结果队列
            try {
                //批次执行问题
                thirdBatchDealBiz.batchDeal(thirdBatchLog.getSourceId(), thirdBatchLog.getBatchNo(), thirdBatchLog.getType(),
                        thirdBatchLog.getAcqRes(), "");
            } catch (Exception e) {
                log.error("financePlanProvider againVerifyFinanceTransfer 批次处理执行异常:", e);
            }
            log.info("重新触发即信批次回调处理结束");
            return false;
        }

        Specification<TransferBuyLog> tbls = Specifications
                .<TransferBuyLog>and()
                .eq("transferId", transfer.getId())
                .eq("state", 0)
                .eq("del", 0)
                .build();
        List<TransferBuyLog> transferBuyLogList = transferBuyLogService.findList(tbls);/* 购买债权转让记录 */
        Preconditions.checkState(!CollectionUtils.isEmpty(transferBuyLogList), "理财计划批量债权转让：购买债权记录不存在!");
        Tender parentTender = tenderService.findById(transfer.getTenderId());/* 转让投资记录 */
        Preconditions.checkNotNull(parentTender, "理财计划批量债权转让: 债权原始投标信息为空!");
        UserThirdAccount transferUserThirdAccount = userThirdAccountService.findByUserId(transfer.getUserId());/* 债权转让人开户信息 */
        Preconditions.checkNotNull(transferUserThirdAccount, "理财计划债权转让人开户记录不存在!");
        Borrow parentBorrow = borrowService.findById(transfer.getBorrowId());
        Preconditions.checkNotNull(parentBorrow, "理财计划债权转让原借款记录不存在!");

        log.info(String.format("复审: 理财计划批量债权转让申请开始: %s", GSON.toJson(msg)));

        //登记存管债权转让
        ImmutableList<Object> result = registerFinanceThirdTransferTender(transfer, transferBuyLogList, parentTender, transferUserThirdAccount, parentBorrow, isRepurchase);
        Iterator<Object> iterator = result.iterator();
        String batchNo = StringHelper.toString(iterator.next());
        //增加批次资金变动记录
        addFinanceBatchAssetChange(transferId, transfer, transferBuyLogList, batchNo, isRepurchase);
        //增加successAt时间
        transfer.setSuccessAt(new Date());
        transferService.save(transfer);
        log.info(String.format("复审: 理财计划批量债权转让申请成功: %s", GSON.toJson(msg)));
        return true;
    }


    /**
     * 增加批次资金变动记录
     *
     * @param transferId
     * @param transfer
     * @param transferBuyLogList
     * @param batchNo
     */
    private void addFinanceBatchAssetChange(long transferId, Transfer transfer, List<TransferBuyLog> transferBuyLogList, String batchNo, String isRepurchase) throws ExecutionException {
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        Date nowDate = new Date();
        /* 理财计划债权转让是否是赎回债权 */
        boolean repurchaseFlag = Boolean.valueOf(isRepurchase);
        // 生成理财计划资产变更主记录
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setBatchNo(batchNo);
        batchAssetChange.setSourceId(transferId);
        batchAssetChange.setType(BatchAssetChangeContants.BATCH_FINANCE_CREDIT_INVEST);
        batchAssetChange.setState(0);
        batchAssetChange.setCreatedAt(nowDate);
        batchAssetChange.setUpdatedAt(nowDate);
        batchAssetChange = batchAssetChangeService.save(batchAssetChange);

        long batchAssetChangeId = batchAssetChange.getId();
        // 债权转让人收款 = 转让本金加应收利息
        BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        //理财计划债权转让是否是赎回债权
        long transferPrincipal = transferBuyLogList.stream().filter(transferBuyLog -> transferBuyLog.getState() == 0).mapToLong(TransferBuyLog::getPrincipal).sum();
        if (repurchaseFlag) {
            batchAssetChangeItem.setType(AssetChangeTypeEnum.InvestorsFinanceBatchSellBonds.getLocalType());  // 理财计划购买人出售债权
            batchAssetChangeItem.setAssetType(AssetTypeContants.finance);
        } else {
            batchAssetChangeItem.setType(AssetChangeTypeEnum.platformFinanceBatchSellBonds.getLocalType());  // 平台出售债权
        }
        batchAssetChangeItem.setUserId(transfer.getUserId());
        batchAssetChangeItem.setMoney(transferPrincipal + transfer.getAlreadyInterest());
        batchAssetChangeItem.setRemark(String.format("出售理财计划匹配债权[%s]获得待收本金和应计利息%s元", transfer.getTitle(),
                StringHelper.formatDouble((transferPrincipal + transfer.getAlreadyInterest()), 100D, true)));
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
            //理财计划债权转让是否是赎回债权
            if (repurchaseFlag) {
                batchAssetChangeItem.setType(AssetChangeTypeEnum.platformFinanceBatchBuyClaims.getLocalType());
            } else {
                batchAssetChangeItem.setType(AssetChangeTypeEnum.InvestorsFinanceBatchBuyClaims.getLocalType());
                batchAssetChangeItem.setAssetType(AssetTypeContants.finance);
            }
            batchAssetChangeItem.setUserId(transferBuyLog.getUserId());
            batchAssetChangeItem.setForUserId(transfer.getUserId());
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setMoney(transferBuyLog.getValidMoney());
            batchAssetChangeItem.setRemark(String.format("购买理财计划匹配债权[%s], 成功扣除资金%s元", transfer.getTitle(),
                    StringHelper.formatDouble(transferBuyLog.getValidMoney(), 100D, true)));
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItem.setSourceId(transferBuyLog.getId());
            batchAssetChangeItemService.save(batchAssetChangeItem);
        }
    }

    /**
     * 登记理财计划存管债权转让
     *
     * @param transfer
     * @param transferBuyLogList
     * @param parentTender
     * @param transferUserThirdAccount
     * @param parentBorrow
     * @throws Exception
     */
    private ImmutableList<Object> registerFinanceThirdTransferTender(Transfer transfer, List<TransferBuyLog> transferBuyLogList, Tender parentTender,
                                                                     UserThirdAccount transferUserThirdAccount, Borrow parentBorrow, String isRepurchase) throws Exception {
        Date nowDate = new Date();
        List<CreditInvest> creditInvestList = new ArrayList<>();
        CreditInvest creditInvest = null;
        UserThirdAccount tenderUserThirdAccount = null;
        // 全部有效投标金额
        int sumAmount = 0;
        for (int i = 0; i < transferBuyLogList.size(); i++) {
            TransferBuyLog transferBuyLog = transferBuyLogList.get(i);
            double txFee = 0;
            /* 债权转让购买人存管账户信息 */
            tenderUserThirdAccount = userThirdAccountService.findByUserId(transferBuyLog.getUserId());
            Preconditions.checkNotNull(tenderUserThirdAccount, "投资人开户记录不存在!");
            //购买债权转让有效金额
            double txAmount = MoneyHelper.round(transferBuyLog.getValidMoney(), 0);
            // 全部有效投标金额
            sumAmount += txAmount;
            //购买债权转让有效金额 本金
            long principal = new Double(MoneyHelper.round(transferBuyLog.getPrincipal(), 0)).longValue();

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
        }
        transferBuyLogService.save(transferBuyLogList);

        //批次号
        String batchNo = jixinHelper.getBatchNo();
        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("transferId", transfer.getId());
        acqResMap.put("isRepurchase", isRepurchase);
        //调用存管批次债权转让接口
        BatchCreditInvestReq request = new BatchCreditInvestReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(sumAmount, 100, false));
        request.setTxCounts(StringHelper.toString(creditInvestList.size()));
        request.setSubPacks(GSON.toJson(creditInvestList));
        request.setAcqRes(GSON.toJson(acqResMap));
        request.setChannel(ChannelContant.HTML);
        request.setNotifyURL(javaDomain + "/pub/tender/v2/third/batch/finance/creditinvest/check");
        request.setRetNotifyURL(javaDomain + "/pub/tender/v2/third/batch/finance/creditinvest/run");
        log.info(GSON.toJson(request));
        BatchCreditInvestResp response = jixinManager.send(JixinTxCodeEnum.BATCH_CREDIT_INVEST, request, BatchCreditInvestResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            BatchCancelReq batchCancelReq = new BatchCancelReq();
            batchCancelReq.setBatchNo(batchNo);
            batchCancelReq.setTxAmount(StringHelper.formatDouble(sumAmount, 100, false));
            batchCancelReq.setTxCounts(StringHelper.toString(creditInvestList.size()));
            batchCancelReq.setChannel(ChannelContant.HTML);
            BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
            if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                throw new Exception("即信批次撤销失败!:" + response.getRetMsg());
            }
            log.error(String.format("复审: 批量理财计划债权转让申请失败: %s", response));
            throw new Exception("理财计划批次购买债权失败!:" + response.getRetMsg());
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setTxDate(request.getTxDate());
        thirdBatchLog.setTxTime(request.getTxTime());
        thirdBatchLog.setSeqNo(request.getSeqNo());
        thirdBatchLog.setSourceId(transfer.getId());
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_FINANCE_CREDIT_INVEST);
        thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
        thirdBatchLog.setRemark("理财计划批次购买债权");
        thirdBatchLogService.save(thirdBatchLog);

        ImmutableList<Object> immutableList = ImmutableList.of(batchNo);
        return immutableList;
    }

}
