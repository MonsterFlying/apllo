package com.gofobao.framework.repayment.biz.Impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeReq;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeResp;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayCheckResp;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayRunResp;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvestRun;
import com.gofobao.framework.api.model.batch_lend_pay.*;
import com.gofobao.framework.api.model.batch_repay.BatchRepayCheckResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayRunResp;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailRunResp;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.contants.ThirdDealStatusContrants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchDealLogContants;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoEndTransfer;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/6/8.
 */
@Service
@Slf4j
public class BorrowRepaymentThirdBizImpl implements BorrowRepaymentThirdBiz {

    final Gson GSON = new GsonBuilder().create();
    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    private AssetChangeProvider assetChangeProvider;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private TransferBuyLogService transferBuyLogService;
    @Autowired
    private TransferBiz transferBiz;
    @Autowired
    private BatchAssetChangeService batchAssetChangeService;
    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;
    @Autowired
    private ThirdBatchDealLogBiz thirdBatchDealLogBiz;
    @Autowired
    private UserCacheService userCacheService;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    public static void main(String[] args) {

    }

    /**
     * 非流转标的 理财计划即信批次放款 （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdBatchFinanceLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay) throws Exception {
        Gson gson = new Gson();
        log.info(String.format("理财计划批次放款调用: %s", gson.toJson(voThirdBatchLendRepay)));
        Date nowDate = new Date();
        Long borrowId = voThirdBatchLendRepay.getBorrowId();
        //查询当前借款的所有 状态为1的 tender记录
        Specification<Tender> ts = Specifications.<Tender>and()
                .eq("borrowId", borrowId)
                .eq("status", 1)
                .build();

        List<Tender> tenderList = tenderService.findList(ts);
        Preconditions.checkNotNull(tenderList, "理财计划批次放款调用: 投标记录为空");
        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "理财计划批次放款调用: 标的信息为空 ");
        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());// 收款人存管账户记录
        Preconditions.checkNotNull(takeUserThirdAccount, "理财计划借款人未开户!");

            /*查询受托支付是否成功*/
        TrusteePayQueryReq request = new TrusteePayQueryReq();
        request.setAccountId(takeUserThirdAccount.getAccountId());
        request.setProductId(borrow.getProductId());
        request.setChannel(ChannelContant.HTML);
        TrusteePayQueryResp trusteePayQueryResp = jixinManager.send(JixinTxCodeEnum.TRUSTEE_PAY_QUERY, request, TrusteePayQueryResp.class);
        if (ObjectUtils.isEmpty(trusteePayQueryResp)) {
            throw new Exception("理财计划批次放款调用：受托支付查询失败,msg->" + trusteePayQueryResp.getRetMsg());
        }
        if ("1".equals(trusteePayQueryResp.getState())) {
            /*收款人id*/
            Long takeUserId = borrow.getTakeUserId();
            if (!ObjectUtils.isEmpty(takeUserId)) {
                takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
            }
        }


        List<LendPay> lendPayList = new ArrayList<>();
        LendPay lendPay;
        UserThirdAccount tenderUserThirdAccount;
        double sumTxAmount = 0, validMoney, debtFee;
        for (Tender tender : tenderList) {
            debtFee = 0;
            //投标有效金额
            validMoney = tender.getValidMoney();
            //已经处理过的批次不放即信处理
            if (BooleanHelper.isTrue(tender.getThirdTenderFlag())) {
                continue;
            }
            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            Preconditions.checkNotNull(tenderUserThirdAccount, "理财计划投资人未开户!");

            sumTxAmount += validMoney; //放款总金额

            String lendPayOrderId = JixinHelper.getOrderId(JixinHelper.LEND_REPAY_PREFIX);
            lendPay = new LendPay();
            lendPay.setAccountId(tenderUserThirdAccount.getAccountId());
            lendPay.setAuthCode(tender.getAuthCode());
            lendPay.setBidFee("0");
            lendPay.setDebtFee(StringHelper.formatDouble(debtFee, 100, false));
            lendPay.setOrderId(lendPayOrderId);
            lendPay.setForAccountId(takeUserThirdAccount.getAccountId());
            lendPay.setTxAmount(StringHelper.formatDouble(validMoney, 100, false));
            lendPay.setProductId(borrow.getProductId());
            lendPayList.add(lendPay);
            tender.setThirdLendPayOrderId(lendPayOrderId);
            tender.setUpdatedAt(nowDate);
        }
        tenderService.save(tenderList);

        //批次号
        String batchNo = jixinHelper.getBatchNo();
        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("borrowId", borrowId);

        try {
            BatchLendPayReq batchLendPayReq = new BatchLendPayReq();
            batchLendPayReq.setBatchNo(batchNo);
            batchLendPayReq.setAcqRes(GSON.toJson(acqResMap));
            batchLendPayReq.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/finance/lendrepay/check");
            batchLendPayReq.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/finance/lendrepay/run");
            batchLendPayReq.setTxAmount(StringHelper.formatDouble(sumTxAmount, 100, false));
            batchLendPayReq.setTxCounts(StringHelper.toString(lendPayList.size()));
            batchLendPayReq.setChannel(ChannelContant.HTML);
            batchLendPayReq.setSubPacks(GSON.toJson(lendPayList));
            BatchLendPayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_LEND_REPAY, batchLendPayReq, BatchLendPayResp.class);
            String retCode = response.getRetCode();
            if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.SUCCESS.equals(retCode))) {
                BatchCancelReq batchCancelReq = new BatchCancelReq();
                batchCancelReq.setBatchNo(batchNo);
                batchCancelReq.setTxAmount(StringHelper.formatDouble(sumTxAmount, 100, false));
                batchCancelReq.setChannel(ChannelContant.HTML);
                batchCancelReq.setTxCounts(StringHelper.toString(lendPayList.size()));
                BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
                if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                    throw new Exception("理财计划即信批次撤销失败!");
                }
            }
            if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
                BatchCancelReq batchCancelReq = new BatchCancelReq();
                batchCancelReq.setBatchNo(batchNo);
                batchCancelReq.setChannel(ChannelContant.HTML);
                batchCancelReq.setTxAmount(StringHelper.formatDouble(sumTxAmount, 100, false));
                batchCancelReq.setTxCounts(StringHelper.toString(lendPayList.size()));
                BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
                if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                    throw new Exception("理财计划即信批次撤销失败!");
                }
                throw new Exception("理财计划即信批次放款失败!");
            }

            //新增放款资产变动记录
            addBorrowFinanceLendRepayAssetChange(nowDate, borrowId, borrow, batchNo);

            //记录日志
            ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
            thirdBatchLog.setBatchNo(batchNo);
            thirdBatchLog.setCreateAt(nowDate);
            thirdBatchLog.setUpdateAt(nowDate);
            thirdBatchLog.setSourceId(borrowId);
            thirdBatchLog.setType(ThirdBatchLogContants.BATCH_FINANCE_LEND_REPAY);
            thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
            thirdBatchLog.setRemark("理财计划即信批次放款");
            thirdBatchLog = thirdBatchLogService.save(thirdBatchLog);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(thirdBatchLog.getBatchNo(), thirdBatchLog.getSourceId(),
                    ThirdBatchDealLogContants.SEND_REQUEST, true, ThirdBatchLogContants.BATCH_LEND_REPAY, "");

            //改变批次放款状态 处理中
            borrow.setLendRepayStatus(ThirdDealStatusContrants.DISPOSING);
            borrowService.save(borrow);
            return ResponseEntity.ok(VoBaseResp.ok("理财计划放款申请成功!"));
        } catch (Exception e) {
            BatchCancelReq batchCancelReq = new BatchCancelReq();
            batchCancelReq.setBatchNo(batchNo);
            batchCancelReq.setTxAmount(StringHelper.formatDouble(0, 100, false));
            batchCancelReq.setChannel(ChannelContant.HTML);
            batchCancelReq.setTxCounts(StringHelper.toString(lendPayList.size()));
            BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
            if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                throw new Exception("理财计划即信批次撤销失败!");
            }
            throw new Exception(e);
        }
    }

    /**
     * 理财计划新增借款放款资产变动
     *
     * @param nowDate
     * @param borrowId
     * @param borrow
     * @param batchNo
     * @throws ExecutionException
     */
    private void addBorrowFinanceLendRepayAssetChange(Date nowDate, Long borrowId, Borrow borrow, String batchNo) throws ExecutionException {
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        long takeUserId = ObjectUtils.isEmpty(borrow.getTakeUserId()) ? borrow.getUserId() : borrow.getTakeUserId();
        //生成借款人资产变动记录
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setSourceId(borrowId);
        batchAssetChange.setState(0);
        batchAssetChange.setType(BatchAssetChangeContants.BATCH_FINANCE_LEND_REPAY);
        batchAssetChange.setCreatedAt(new Date());
        batchAssetChange.setUpdatedAt(new Date());
        batchAssetChange.setBatchNo(batchNo);
        batchAssetChangeService.save(batchAssetChange);
        long batchAssetChangeId = batchAssetChange.getId();
        // 借款人还款
        // 放款
        BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setSourceId(borrow.getId());
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItem.setMoney(borrow.getMoney());
        batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
        batchAssetChangeItem.setRemark(String.format("理财计划标的[%s]融资成功. 放款%s元", borrow.getName(), StringHelper.formatDouble(borrow.getMoney() / 100D, true)));
        batchAssetChangeItem.setType(AssetChangeTypeEnum.financeBorrow.getLocalType());
        batchAssetChangeItem.setUserId(takeUserId);
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItemService.save(batchAssetChangeItem);
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchFinanceLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchLendPayCheckResp lendRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchLendPayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(lendRepayCheckResp)) {
            log.error("=============================理财计划即信批次放款检验参数回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(lendRepayCheckResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long borrowId = NumberHelper.toLong(acqResMap.get("borrowId"));
        if (!JixinResultContants.SUCCESS.equals(lendRepayCheckResp.getRetCode())) {
            log.error("=============================理财计划即信批次放款检验参数回调===========================");
            log.error("回调失败! msg:" + lendRepayCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), borrowId, 2, ThirdBatchLogContants.BATCH_LEND_REPAY);
            //改变批次放款状态 处理失败
            Borrow borrow = borrowService.findById(borrowId);
            borrow.setLendRepayStatus(ThirdDealStatusContrants.INDISPOSE);
            borrowService.save(borrow);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(lendRepayCheckResp.getBatchNo(), borrowId, ThirdBatchDealLogContants.PARAM_CHECK, false,
                    ThirdBatchLogContants.BATCH_LEND_REPAY, lendRepayCheckResp.getRetMsg());
        } else {
            log.info("=============================理财计划即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), borrowId, 1, ThirdBatchLogContants.BATCH_LEND_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(lendRepayCheckResp.getBatchNo(), borrowId, ThirdBatchDealLogContants.PARAM_CHECK, true,
                    ThirdBatchLogContants.BATCH_LEND_REPAY, lendRepayCheckResp.getRetMsg());
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchFinanceLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchLendPayRunResp lendRepayRunResp = jixinManager.callback(request, new TypeToken<BatchLendPayRunResp>() {
        });
        return dealBatchFinanceLendRepay(lendRepayRunResp);
    }

    /**
     * 处理理财计划即信批次放款
     *
     * @param lendRepayRunResp
     * @return
     */
    public ResponseEntity<String> dealBatchFinanceLendRepay(BatchLendPayRunResp lendRepayRunResp) {
        log.info("进入理财计划批次放款处理流程!");
        Preconditions.checkNotNull(lendRepayRunResp, "即信请求体为空！");
        log.info("即信请求体:", GSON.toJson(lendRepayRunResp));
        Preconditions.checkState(JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode()), "即信回调反馈：处理失败! msg:" + lendRepayRunResp.getRetMsg());
        Map<String, Object> acqResMap = GSON.fromJson(lendRepayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);


        //触发处理理财计划批次放款处理结果队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("borrowId")), lendRepayRunResp.getBatchNo(), lendRepayRunResp.getAcqRes(), GSON.toJson(lendRepayRunResp));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 非流转标的 即信批次放款 （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay) throws Exception {
        Gson gson = new Gson();
        log.info(String.format("批次放款调用: %s", gson.toJson(voThirdBatchLendRepay)));
        Date nowDate = new Date();
        Long borrowId = voThirdBatchLendRepay.getBorrowId();
        //查询当前借款的所有 状态为1的 tender记录
        Specification<Tender> ts = Specifications.<Tender>and()
                .eq("borrowId", borrowId)
                .eq("status", 1)
                .build();

        List<Tender> tenderList = tenderService.findList(ts);
        Preconditions.checkNotNull(tenderList, "批次放款调用: 投标记录为空");

        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "批次放款调用: 标的信息为空 ");

        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());// 收款人存管账户记录
        Preconditions.checkNotNull(takeUserThirdAccount, "借款人未开户!");

        /*查询受托支付是否成功*/
        TrusteePayQueryReq trusteePayQueryReq = new TrusteePayQueryReq();
        trusteePayQueryReq.setAccountId(takeUserThirdAccount.getAccountId());
        trusteePayQueryReq.setProductId(borrow.getProductId());
        trusteePayQueryReq.setChannel(ChannelContant.HTML);
        TrusteePayQueryResp trusteePayQueryResp = jixinManager.send(JixinTxCodeEnum.TRUSTEE_PAY_QUERY, trusteePayQueryReq, TrusteePayQueryResp.class);
        if (ObjectUtils.isEmpty(trusteePayQueryResp)) {
            throw new Exception("批次放款调用：受托支付查询失败,msg->" + trusteePayQueryResp.getRetMsg());
        }

        if ("1".equals(trusteePayQueryResp.getState())) {
                     /*收款人id*/
            Long takeUserId = borrow.getTakeUserId();
            if (!ObjectUtils.isEmpty(takeUserId)) {
                takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
            }
        }

        double totalManageFee = 0; // 净值标, 收取账户管理费
        if (borrow.getType() == 1) {
            double manageFeeRate = 0.0012;
            if (borrow.getRepayFashion() == 1) {
                totalManageFee = MoneyHelper.round(borrow.getMoney() * manageFeeRate / 30 * borrow.getTimeLimit(), 0);
            } else {
                totalManageFee = MoneyHelper.round(borrow.getMoney() * manageFeeRate * borrow.getTimeLimit(), 0);
            }
        }

        List<LendPay> lendPayList = new ArrayList<>();
        LendPay lendPay;
        UserThirdAccount tenderUserThirdAccount;
        double sumTxAmount = 0, validMoney, debtFee;
        double sumNetWorthFee = 0;
        for (Tender tender : tenderList) {
            debtFee = 0;
            //投标有效金额
            validMoney = tender.getValidMoney();
                /*净值管理费*/
            double newWorthFee = MoneyHelper.round(MoneyHelper.multiply(MoneyHelper.divide(validMoney, borrow.getMoney()), totalManageFee), 0);
            //净值账户管理费
            if (borrow.getType() == 1) {
                sumNetWorthFee = MoneyHelper.add(sumNetWorthFee, newWorthFee);
            }
            //已经处理过的批次不放即信处理
            if (BooleanHelper.isTrue(tender.getThirdTenderFlag())) {
                continue;
            }
            //即信收取净值账户管理费
            if (borrow.getType() == 1) {
                debtFee = MoneyHelper.add(debtFee, newWorthFee);
            }
            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            Preconditions.checkNotNull(tenderUserThirdAccount, "投资人未开户!");

            sumTxAmount += validMoney; //放款总金额

            String lendPayOrderId = JixinHelper.getOrderId(JixinHelper.LEND_REPAY_PREFIX);
            lendPay = new LendPay();
            lendPay.setAccountId(tenderUserThirdAccount.getAccountId());
            lendPay.setAuthCode(tender.getAuthCode());
            lendPay.setBidFee("0");
            lendPay.setDebtFee(StringHelper.formatDouble(debtFee, 100, false));
            lendPay.setOrderId(lendPayOrderId);
            lendPay.setForAccountId(takeUserThirdAccount.getAccountId());
            lendPay.setTxAmount(StringHelper.formatDouble(validMoney, 100, false));
            lendPay.setProductId(borrow.getProductId());
            lendPayList.add(lendPay);
            tender.setThirdLendPayOrderId(lendPayOrderId);
            tender.setUpdatedAt(nowDate);
        }
        tenderService.save(tenderList);

        //批次号
        String batchNo = jixinHelper.getBatchNo();
        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("borrowId", borrowId);

        try {
            BatchLendPayReq batchLendPayReq = new BatchLendPayReq();
            batchLendPayReq.setBatchNo(batchNo);
            batchLendPayReq.setAcqRes(GSON.toJson(acqResMap));
            batchLendPayReq.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/lendrepay/check");
            batchLendPayReq.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/lendrepay/run");
            batchLendPayReq.setTxAmount(StringHelper.formatDouble(sumTxAmount, 100, false));
            batchLendPayReq.setChannel(ChannelContant.HTML);
            batchLendPayReq.setTxCounts(StringHelper.toString(lendPayList.size()));
            batchLendPayReq.setSubPacks(GSON.toJson(lendPayList));
            BatchLendPayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_LEND_REPAY, batchLendPayReq, BatchLendPayResp.class);
            String retCode = response.getRetCode();
            if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.SUCCESS.equals(retCode))) {
                BatchCancelReq batchCancelReq = new BatchCancelReq();
                batchCancelReq.setBatchNo(batchNo);
                batchCancelReq.setTxAmount(StringHelper.formatDouble(sumTxAmount, 100, false));
                batchCancelReq.setTxCounts(StringHelper.toString(lendPayList.size()));
                batchCancelReq.setChannel(ChannelContant.HTML);
                BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
                if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                    throw new Exception("即信批次撤销失败!");
                }
            }
            if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
                BatchCancelReq batchCancelReq = new BatchCancelReq();
                batchCancelReq.setBatchNo(batchNo);
                batchCancelReq.setTxAmount(StringHelper.formatDouble(sumTxAmount, 100, false));
                batchCancelReq.setTxCounts(StringHelper.toString(lendPayList.size()));
                batchCancelReq.setChannel(ChannelContant.HTML);
                BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
                if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                    throw new Exception("即信批次撤销失败!");
                }
                throw new Exception("即信批次放款失败!");
            }

            //新增放款资产变动记录
            addBorrowLendRepayAssetChange(nowDate, borrowId, borrow, sumNetWorthFee, batchNo);
            //将新手投标用户置为已投状态
            recordUserNoviceTender(borrow, tenderList);

            //记录日志
            ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
            thirdBatchLog.setBatchNo(batchNo);
            thirdBatchLog.setCreateAt(nowDate);
            thirdBatchLog.setUpdateAt(nowDate);
            thirdBatchLog.setSourceId(borrowId);
            thirdBatchLog.setType(ThirdBatchLogContants.BATCH_LEND_REPAY);
            thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
            thirdBatchLog.setRemark("即信批次放款");
            thirdBatchLog = thirdBatchLogService.save(thirdBatchLog);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(thirdBatchLog.getBatchNo(), thirdBatchLog.getSourceId(),
                    ThirdBatchDealLogContants.SEND_REQUEST, true, ThirdBatchLogContants.BATCH_LEND_REPAY, "");

            //改变批次放款状态 处理中
            borrow.setLendRepayStatus(ThirdDealStatusContrants.DISPOSING);
            borrowService.save(borrow);
            return ResponseEntity.ok(VoBaseResp.ok("放款申请成功!"));
        } catch (Exception e) {
            BatchCancelReq batchCancelReq = new BatchCancelReq();
            batchCancelReq.setBatchNo(batchNo);
            batchCancelReq.setTxAmount(StringHelper.formatDouble(0, 100, false));
            batchCancelReq.setTxCounts(StringHelper.toString(lendPayList.size()));
            batchCancelReq.setChannel(ChannelContant.HTML);
            BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
            if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                throw new Exception("即信批次撤销失败!");
            }
            throw new Exception(e);
        }
    }

    /**
     * 标记新手投标标识
     *
     * @param borrow
     * @param tenderList
     */
    private void recordUserNoviceTender(Borrow borrow, List<Tender> tenderList) throws Exception {
        for (Tender tender : tenderList) {
            log.info(String.format("标记新手投标标识"));
            UserCache userCache = userCacheService.findById(tender.getUserId());

            if (borrow.isTransfer() && (!BooleanUtils.toBoolean(userCache.getTenderTransfer()))) {
                userCache.setTenderTransfer(borrow.getId().intValue());
            } else if ((borrow.getType() == 0) && (!BooleanUtils.toBoolean(userCache.getTenderTuijian()))) {
                userCache.setTenderTuijian(borrow.getId().intValue());
            } else if ((borrow.getType() == 1) && (!BooleanUtils.toBoolean(userCache.getTenderJingzhi()))) {
                userCache.setTenderJingzhi(borrow.getId().intValue());
            } else if ((borrow.getType() == 2) && (!BooleanUtils.toBoolean(userCache.getTenderMiao()))) {
                userCache.setTenderMiao(borrow.getId().intValue());
            } else if ((borrow.getType() == 4) && (!BooleanUtils.toBoolean(userCache.getTenderQudao()))) {
                userCache.setTenderQudao(borrow.getId().intValue());
            }
            userCacheService.save(userCache);
        }
    }

    /**
     * 新增借款放款资产变动
     *
     * @param nowDate
     * @param borrowId
     * @param borrow
     * @param sumNetWorthFee
     * @param batchNo
     * @throws ExecutionException
     */
    private void addBorrowLendRepayAssetChange(Date nowDate, Long borrowId, Borrow borrow, double sumNetWorthFee, String batchNo) throws ExecutionException {
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        long takeUserId = ObjectUtils.isEmpty(borrow.getTakeUserId()) ? borrow.getUserId() : borrow.getTakeUserId();
        // 获取待还
        long feeId = assetChangeProvider.getFeeAccountId();  // 收费账户
        //生成借款人资产变动记录
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setSourceId(borrowId);
        batchAssetChange.setState(0);
        batchAssetChange.setType(BatchAssetChangeContants.BATCH_LEND_REPAY);
        batchAssetChange.setCreatedAt(new Date());
        batchAssetChange.setUpdatedAt(new Date());
        batchAssetChange.setBatchNo(batchNo);
        batchAssetChangeService.save(batchAssetChange);
        long batchAssetChangeId = batchAssetChange.getId();
        // 借款人还款
        // 放款
        BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setSourceId(borrow.getId());
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItem.setMoney(borrow.getMoney());
        batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
        batchAssetChangeItem.setRemark(String.format("标的[%s]融资成功. 放款%s元", borrow.getName(), StringHelper.formatDouble(borrow.getMoney() / 100D, true)));
        batchAssetChangeItem.setType(AssetChangeTypeEnum.borrow.getLocalType());
        batchAssetChangeItem.setUserId(takeUserId);
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItemService.save(batchAssetChangeItem);

        // 净值账户管理费
        if (borrow.getType() == 1) {
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setSourceId(borrow.getId());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItem.setMoney(new Double(sumNetWorthFee).longValue());
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setRemark(String.format("扣除标的[%s]融资管理费%s元", borrow.getName(), StringHelper.formatDouble(MoneyHelper.divide(new Double(sumNetWorthFee).longValue(), 100D), true)));
            batchAssetChangeItem.setType(AssetChangeTypeEnum.financingManagementFee.getLocalType());
            batchAssetChangeItem.setUserId(takeUserId);
            batchAssetChangeItem.setForUserId(feeId);
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItemService.save(batchAssetChangeItem);  // 扣除融资管理费

            // 费用平台添加收取的转让费
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setSourceId(borrow.getId());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItem.setMoney(new Double(sumNetWorthFee).longValue());
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setRemark(String.format("收取标的[%s]融资管理费%s元", borrow.getName(), StringHelper.formatDouble(MoneyHelper.divide(new Double(sumNetWorthFee).longValue(), 100D), true)));
            batchAssetChangeItem.setType(AssetChangeTypeEnum.platformFinancingManagementFee.getLocalType());
            batchAssetChangeItem.setUserId(feeId);
            batchAssetChangeItem.setForUserId(takeUserId);
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItemService.save(batchAssetChangeItem);  // 收取融资管理费
        }
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayCheckResp repayCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(repayCheckResp)) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(repayCheckResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        long repaymentId = NumberHelper.toLong(acqResMap.get("repaymentId"));
        if (!JixinResultContants.SUCCESS.equals(repayCheckResp.getRetCode())) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("回调失败! msg:" + repayCheckResp.getRetMsg());
            /* 还款会员id */
            long userId = NumberHelper.toLong(acqResMap.get("userId"));
            UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(userId);
            String freezeOrderId = StringHelper.toString(acqResMap.get("freezeOrderId"));
            String freezeMoney = StringHelper.formatDouble(MoneyHelper.round(NumberHelper.toDouble(acqResMap.get("freezeMoney")), 2), false);//元

            //解除存管资金冻结
            String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);
            BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
            balanceUnfreezeReq.setAccountId(borrowUserThirdAccount.getAccountId());
            balanceUnfreezeReq.setTxAmount(freezeMoney);
            balanceUnfreezeReq.setChannel(ChannelContant.HTML);
            balanceUnfreezeReq.setOrderId(orderId);
            balanceUnfreezeReq.setOrgOrderId(freezeOrderId);
            BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
            if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
                log.error("===========================================================================");
                log.error("即信批次还款解除冻结资金失败：" + balanceUnfreezeResp.getRetMsg());
                log.error("===========================================================================");
                return ResponseEntity.ok("error");
            }
            //立即还款冻结
            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.unfreeze);  // 招标失败解除冻结资金
            assetChange.setUserId(userId);
            assetChange.setMoney(new Double(NumberHelper.toDouble(freezeMoney) * 100).longValue());
            assetChange.setRemark("即信批次还款解除冻结可用资金");
            assetChange.setSourceId(repaymentId);
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error("即信批次还款解除冻结可用资金异常:", e);
            }

            //更新即信放款状态 为处理失败!
            BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);
            borrowRepayment.setRepayStatus(ThirdDealStatusContrants.UNDISPOSED);
            borrowRepayment.setUpdatedAt(new Date());
            borrowRepaymentService.save(borrowRepayment);

            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), repaymentId, 2, ThirdBatchLogContants.BATCH_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(repayCheckResp.getBatchNo(), repaymentId,
                    ThirdBatchDealLogContants.PARAM_CHECK, false, ThirdBatchLogContants.BATCH_REPAY, "");
        } else {
            log.info("=============================即信批次还款检验参数回调===========================");
            log.info("回调成功!");
            try {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), repaymentId, 1, ThirdBatchLogContants.BATCH_REPAY);
                //记录批次处理日志
                thirdBatchDealLogBiz.recordThirdBatchDealLog(repayCheckResp.getBatchNo(), repaymentId,
                        ThirdBatchDealLogContants.PARAM_CHECK, true, ThirdBatchLogContants.BATCH_REPAY, GSON.toJson(repayCheckResp));
            } catch (Exception e) {
                log.error("更新批次日志记录失败:", e);
            }
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        log.info("即信批次回调触发");
        BatchRepayRunResp repayRunResp = jixinManager.callback(request, new TypeToken<BatchRepayRunResp>() {
        });
        return dealBatchRepay(repayRunResp);
    }

    /**
     * 处理即信批次还款
     *
     * @param repayRunResp
     * @return
     */
    public ResponseEntity<String> dealBatchRepay(BatchRepayRunResp repayRunResp) {
        Preconditions.checkNotNull(repayRunResp, "即信批次回调触发: 请求体为空!");
        Preconditions.checkState(JixinResultContants.SUCCESS.equals(repayRunResp.getRetCode()), String.format("即信批次回调触发: 验证失败 %s", repayRunResp.getRetMsg()));

        Map<String, Object> acqResMap = GSON.fromJson(repayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        //触发处理批次放款处理结果队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("repaymentId")), StringHelper.toString(repayRunResp.getBatchNo()), repayRunResp.getAcqRes(), GSON.toJson(repayRunResp));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }
        log.info("即信批次回调处理结束");
        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchLendPayCheckResp lendRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchLendPayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(lendRepayCheckResp)) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(lendRepayCheckResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long borrowId = NumberHelper.toLong(acqResMap.get("borrowId"));
        if (!JixinResultContants.SUCCESS.equals(lendRepayCheckResp.getRetCode())) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("回调失败! msg:" + lendRepayCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), borrowId, 2, ThirdBatchLogContants.BATCH_LEND_REPAY);
            //改变批次放款状态 处理失败
            Borrow borrow = borrowService.findById(borrowId);
            borrow.setLendRepayStatus(ThirdDealStatusContrants.INDISPOSE);
            borrowService.save(borrow);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(lendRepayCheckResp.getBatchNo(), borrowId, ThirdBatchDealLogContants.PARAM_CHECK, false,
                    ThirdBatchLogContants.BATCH_LEND_REPAY, lendRepayCheckResp.getRetMsg());
        } else {
            log.info("=============================即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), borrowId, 1, ThirdBatchLogContants.BATCH_LEND_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(lendRepayCheckResp.getBatchNo(), borrowId, ThirdBatchDealLogContants.PARAM_CHECK, true,
                    ThirdBatchLogContants.BATCH_LEND_REPAY, lendRepayCheckResp.getRetMsg());
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchLendPayRunResp lendRepayRunResp = jixinManager.callback(request, new TypeToken<BatchLendPayRunResp>() {
        });
        return dealBatchLendRepay(lendRepayRunResp);
    }

    /**
     * 处理即信批次放款
     *
     * @param lendRepayRunResp
     * @return
     */
    public ResponseEntity<String> dealBatchLendRepay(BatchLendPayRunResp lendRepayRunResp) {
        log.info("进入批次放款处理流程!");
        Preconditions.checkNotNull(lendRepayRunResp, "即信请求体为空！");
        log.info("即信请求体:", GSON.toJson(lendRepayRunResp));
        Preconditions.checkState(JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode()), "即信回调反馈：处理失败! msg:" + lendRepayRunResp.getRetMsg());
        Map<String, Object> acqResMap = GSON.fromJson(lendRepayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);


        //触发处理批次放款处理结果队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("borrowId")), lendRepayRunResp.getBatchNo(), lendRepayRunResp.getAcqRes(), GSON.toJson(lendRepayRunResp));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 批次名义借款人垫付参数检查回调
     */
    public ResponseEntity<String> thirdBatchAdvanceCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayCheckResp batchBailRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchBailRepayCheckResp>() {
        });
        if (ObjectUtils.isEmpty(batchBailRepayCheckResp)) {
            log.error("=============================批次名义借款人垫付参数检查回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(batchBailRepayCheckResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long repaymentId = NumberHelper.toLong(acqResMap.get("repaymentId"));
        if (!JixinResultContants.SUCCESS.equals(batchBailRepayCheckResp.getRetCode())) {
            log.error("=============================批次名义借款人垫付参数检查回调===========================");
            log.error("回调失败! msg:" + batchBailRepayCheckResp.getRetMsg());
            BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);

            //垫付失败解冻账户资金
            if (unfreezeAssetByAdvance(batchBailRepayCheckResp, acqResMap, repaymentId)) {
                return ResponseEntity.ok("error");
            }
            //取消垫付债权转让
            cancelAdvanceTransfer(borrowRepayment);

            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), repaymentId, 2, ThirdBatchLogContants.BATCH_BAIL_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(batchBailRepayCheckResp.getBatchNo(), repaymentId,
                    ThirdBatchDealLogContants.PARAM_CHECK, false, ThirdBatchLogContants.BATCH_BAIL_REPAY, GSON.toJson(batchBailRepayCheckResp));
        } else {
            log.info("=============================批次名义借款人垫付参数成功回调===========================");
            log.info("回调成功!");
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), repaymentId, 1, ThirdBatchLogContants.BATCH_BAIL_REPAY);
            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(batchBailRepayCheckResp.getBatchNo(), repaymentId,
                    ThirdBatchDealLogContants.PARAM_CHECK, true, ThirdBatchLogContants.BATCH_BAIL_REPAY, "");
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 取消垫付债权转让
     *
     * @param borrowRepayment
     */
    private void cancelAdvanceTransfer(BorrowRepayment borrowRepayment) {
    /* 查询投资列表 */
        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowRepayment.getBorrowId())
                .build();
        List<Tender> tenderList = tenderService.findList(specification);
        Preconditions.checkNotNull(tenderList, "投资人投标信息不存在!");
            /* 投资记录id集合 */
        List<Long> tenderIds = tenderList.stream().map(tender -> tender.getId()).collect(Collectors.toList());
        Map<Long, Tender> tenderMaps = tenderList.stream().collect(Collectors.toMap(Tender::getId, Function.identity()));
            /* 查询未转让的回款记录 */
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("order", borrowRepayment.getOrder())
                .eq("transferFlag", 0)
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Preconditions.checkNotNull("投资回款记录不存在!");
        //查询债权转让记录
        Specification<Transfer> transferSpecification = Specifications
                .<Transfer>and()
                .in("tenderId", tenderIds.toArray())
                .build();
        List<Transfer> transferList = transferService.findList(transferSpecification);
        Preconditions.checkState(!CollectionUtils.isEmpty(transferList), "债权转让记录不存在!");
        List<Long> transferIds = transferList.stream().map(Transfer::getId).collect(Collectors.toList());
        Map<Long/* 投标id */, Transfer> transferMaps = transferList.stream().collect(Collectors.toMap(Transfer::getTenderId, Function.identity()));
            /* 购买债权转让记录 */
        Specification<TransferBuyLog> tbls = Specifications
                .<TransferBuyLog>and()
                .in("transferId", transferIds.toArray())
                .build();
        List<TransferBuyLog> transferBuyLogList = transferBuyLogService.findList(tbls);
        Preconditions.checkState(!CollectionUtils.isEmpty(transferBuyLogList), "购买债权转让记录不能为空!");
        //
        borrowCollectionList.stream().forEach(borrowCollection -> {
            //更新tender状态
            Tender tender = tenderMaps.get(borrowCollection.getTenderId());
            tender.setTransferFlag(0);

            tender.setUpdatedAt(new Date());
            //取消债权转让
            Transfer transfer = transferMaps.get(tender.getId());
            VoEndTransfer voEndTransfer = new VoEndTransfer();
            voEndTransfer.setUserId(transfer.getUserId());
            voEndTransfer.setTransferId(transfer.getId());
            try {
                transferBiz.endTransfer(voEndTransfer);
            } catch (Exception e) {
                log.error("borrowRepaymentThirdBiz thirdBatchAdvanceCheckCall error:", e);
            }
        });
        tenderService.save(tenderList);
    }

    /**
     * 垫付失败解冻账户资金
     *
     * @param batchBailRepayCheckResp
     * @param acqResMap
     * @param repaymentId
     * @return
     */
    private boolean unfreezeAssetByAdvance(BatchBailRepayCheckResp batchBailRepayCheckResp, Map<String, Object> acqResMap, Long repaymentId) {
        String accountId = StringHelper.toString(acqResMap.get("accountId"));//担保人账户id
        String txAmount = batchBailRepayCheckResp.getTxAmount();
        UserThirdAccount titularUserThirdAccount = userThirdAccountService.findByAccountId(accountId);//担保人存管信息

        //解除本地冻结
        //立即还款冻结
        long frozenMoney = new Double(MoneyHelper.multiply(NumberHelper.toDouble(txAmount), 100)).longValue();
        AssetChange assetChange = new AssetChange();
        assetChange.setType(AssetChangeTypeEnum.unfreeze);  // 招标失败解除冻结资金
        assetChange.setUserId(titularUserThirdAccount.getUserId());
        assetChange.setMoney(frozenMoney);
        assetChange.setRemark("名义借款人垫付解除冻结可用资金");
        assetChange.setSourceId(repaymentId);
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error("名义借款人垫付解除冻结可用资金异常:", e);
        }
        return false;
    }

    /**
     * 批次名义借款人垫付业务处理回调
     */
    public ResponseEntity<String> thirdBatchAdvanceRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayRunResp batchBailRepayRunResp = jixinManager.callback(request, new TypeToken<BatchBailRepayRunResp>() {
        });
        return dealBatchAdvance(batchBailRepayRunResp);
    }

    /**
     * 处理批次名义借款人垫付处理
     *
     * @param batchBailRepayRunResp
     * @return
     */
    public ResponseEntity<String> dealBatchAdvance(BatchBailRepayRunResp batchBailRepayRunResp) {

        log.info("进入批次名义借款人垫付业务处理流程");
        log.info("请求参数：" + GSON.toJson(batchBailRepayRunResp));
        Preconditions.checkNotNull(batchBailRepayRunResp, "批次名义借款人垫付业务处理回调，请求体为空!");
        Preconditions.checkState(JixinResultContants.SUCCESS.equals(batchBailRepayRunResp.getRetCode()), "批次名义借款人垫付业务处理回调：回调失败！ msg:" + batchBailRepayRunResp.getRetMsg());
        Map<String, Object> acqResMap = GSON.fromJson(batchBailRepayRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        //=============================================
        // 保存批次名义借款人垫付授权号
        //=============================================
        try {
            List<CreditInvestRun> creditInvestRunList = GSON.fromJson(batchBailRepayRunResp.getSubPacks(), new TypeToken<List<CreditInvestRun>>() {
            }.getType());
            saveThirdTransferAuthCode(creditInvestRunList);
        } catch (JsonSyntaxException e) {
            log.error("保存批次名义借款人垫付授权号!", e);
        }

        //触发批次名义借款人垫付业务处理队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("borrowId")), batchBailRepayRunResp.getBatchNo(), batchBailRepayRunResp.getAcqRes(), GSON.toJson(batchBailRepayRunResp));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }

        /*// 触发批次名义借款人垫付业务处理队列
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("repaymentId")),
                        MqConfig.BATCH_NO, StringHelper.toString(batchBailRepayRunResp.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("borrowRepaymentThirdBizImpl thirdBatchAdvanceRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("borrowRepaymentThirdBizImpl thirdBatchAdvanceRunCall send mq exception", e);
        }*/

        return ResponseEntity.ok("success");
    }

    /**
     * 保存批次名义借款人授权号
     *
     * @param creditInvests
     */
    public void saveThirdTransferAuthCode(List<CreditInvestRun> creditInvests) {
        List<String> orderList = creditInvests.stream().map(creditInvest -> creditInvest.getOrderId()).collect(Collectors.toList());
        Specification<TransferBuyLog> bcs = Specifications
                .<TransferBuyLog>and()
                .eq("thirdTransferOrderId", orderList.toArray())
                .build();
        int pageIndex = 0;
        int maxPageSize = 50;
        Pageable pageable = null;
        List<TransferBuyLog> transferBuyLogList = null;
        do {
            pageable = new PageRequest(pageIndex++, maxPageSize, new Sort(Sort.Direction.ASC, "id"));
            transferBuyLogList = transferBuyLogService.findList(bcs, pageable);
            if (CollectionUtils.isEmpty(transferBuyLogList)) {
                break;
            }

            Map<String, TransferBuyLog> transferBuyLogMaps = transferBuyLogList.stream().collect(Collectors.toMap(TransferBuyLog::getThirdTransferOrderId, Function.identity()));
            creditInvests.stream().forEach(creditInvest -> {
                TransferBuyLog transferBuyLog = transferBuyLogMaps.get(creditInvest.getOrderId());
                transferBuyLog.setTransferAuthCode(creditInvest.getAuthCode());
            });
            transferBuyLogService.save(transferBuyLogList);
        } while (transferBuyLogList.size() >= maxPageSize);
    }

    /**
     * 批次融资人还名义借款人账户垫款业务处理回调
     *
     * @param request
     * @param response
     */
    public ResponseEntity<String> thirdBatchRepayAdvanceRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayBailRunResp batchRepayBailRunResp = jixinManager.callback(request, new TypeToken<BatchRepayBailRunResp>() {
        });
        Map<String, Object> acqResMap = GSON.fromJson(batchRepayBailRunResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        if (ObjectUtils.isEmpty(batchRepayBailRunResp)) {
            log.error("========================================批次回调============================================");
            log.error("=============================批次融资人还名义借款人账户垫款业务处理回调===========================");
            log.error("请求体为空!");
            log.error("============================================================================================");
            log.error("============================================================================================");
        }

        if (!JixinResultContants.SUCCESS.equals(batchRepayBailRunResp.getRetCode())) {
            log.error("========================================批次回调============================================");
            log.error("=============================批次融资人还名义借款人账户垫款业务处理回调===========================");
            log.error("回调失败! msg:" + batchRepayBailRunResp.getRetMsg());
            log.error("============================================================================================");
            log.error("============================================================================================");
        } else {
            log.error("========================================批次回调============================================");
            log.error("=============================批次融资人还名义借款人账户垫款业务处理回调===========================");
            log.info("回调成功!");
            log.error("============================================================================================");
            log.error("============================================================================================");
        }

        //触发处理批次处理结果队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("repaymentId")), batchRepayBailRunResp.getBatchNo(),
                    batchRepayBailRunResp.getAcqRes(), GSON.toJson(batchRepayBailRunResp));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }
        /*
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("repaymentId")),
                        MqConfig.ACQ_RES, batchRepayBailRunResp.getAcqRes(),
                        MqConfig.BATCH_NO, StringHelper.toString(batchRepayBailRunResp.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayAdvanceRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayAdvanceRunCall send mq exception", e);
        }*/

        return ResponseEntity.ok("success");
    }

}
