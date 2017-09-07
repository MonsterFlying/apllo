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
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvestRun;
import com.gofobao.framework.api.model.batch_lend_pay.*;
import com.gofobao.framework.api.model.batch_repay.BatchRepayCheckResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayRunResp;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailCheckResp;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailRunResp;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
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
    private JixinManager jixinManager;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private MqHelper mqHelper;
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

    @Value("${gofobao.javaDomain}")
    private String javaDomain;


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
        Long takeUserId = borrow.getTakeUserId();

        /*查询受托支付是否成功*/
        TrusteePayQueryReq request = new TrusteePayQueryReq();
        request.setAccountId(takeUserThirdAccount.getAccountId());
        request.setProductId(borrow.getProductId());
        request.setChannel(ChannelContant.HTML);
        TrusteePayQueryResp trusteePayQueryResp = jixinManager.send(JixinTxCodeEnum.TRUSTEE_PAY_QUERY, request, TrusteePayQueryResp.class);
        if ((ObjectUtils.isEmpty(trusteePayQueryResp)) || (!ObjectUtils.isEmpty(trusteePayQueryResp.getRetCode()) && !JixinResultContants.SUCCESS.equals(trusteePayQueryResp.getRetCode()))) {
            throw new Exception("批次放款调用：受托支付查询失败,msg->" + trusteePayQueryResp.getRetMsg());
        }

        if (!ObjectUtils.isEmpty(takeUserId) && "1".equals(trusteePayQueryResp.getState())) {
            takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
        }

        double totalManageFee = 0; // 净值标, 收取账户管理费
        if (borrow.getType() == 1) {
            double manageFeeRate = 0.0012;
            if (borrow.getRepayFashion() == 1) {
                totalManageFee = MathHelper.myRound(borrow.getMoney() * manageFeeRate / 30 * borrow.getTimeLimit(), 2);
            } else {
                totalManageFee = MathHelper.myRound(borrow.getMoney() * manageFeeRate * borrow.getTimeLimit(), 2);
            }
        }

        List<LendPay> lendPayList = new ArrayList<>();
        LendPay lendPay;
        UserThirdAccount tenderUserThirdAccount;
        double sumCount = 0, validMoney, debtFee;
        for (Tender tender : tenderList) {
            debtFee = 0;
            if (BooleanHelper.isTrue(tender.getThirdTenderFlag())) {
                continue;
            }
            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            Preconditions.checkNotNull(tenderUserThirdAccount, "投资人未开户!");

            validMoney = tender.getValidMoney();//投标有效金额
            sumCount += validMoney; //放款总金额

            //净值账户管理费
            if (borrow.getType() == 1) {
                debtFee += MathHelper.myRound(validMoney / borrow.getMoney().doubleValue() * totalManageFee, 2);
            }

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

        BatchLendPayReq batchLendPayReq = new BatchLendPayReq();
        batchLendPayReq.setBatchNo(batchNo);
        batchLendPayReq.setAcqRes(GSON.toJson(acqResMap));
        batchLendPayReq.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/lendrepay/check");
        batchLendPayReq.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/lendrepay/run");
        batchLendPayReq.setTxAmount(StringHelper.formatDouble(sumCount, 100, false));
        batchLendPayReq.setTxCounts(StringHelper.toString(lendPayList.size()));
        batchLendPayReq.setSubPacks(GSON.toJson(lendPayList));
        batchLendPayReq.setChannel(ChannelContant.HTML);
        BatchLendPayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_LEND_REPAY, batchLendPayReq, BatchLendPayResp.class);
        String retCode = response.getRetCode();
        if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.SUCCESS.equals(retCode))) {
            throw new Exception("即信批次放款失败:" + response.getRetMsg());
        }
        if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            throw new Exception("即信批次放款失败!");
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_LEND_REPAY);
        thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
        thirdBatchLog.setRemark("即信批次放款");
        thirdBatchLogService.save(thirdBatchLog);
        return null;
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
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), repaymentId, 2);
            long userId = NumberHelper.toLong(acqResMap.get("userId"));
            UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(userId);
            String freezeOrderId = StringHelper.toString(acqResMap.get("freezeOrderId"));
            String freezeMoney = StringHelper.toString(acqResMap.get("freezeMoney"));//分

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
            assetChange.setGroupSeqNo(assetChangeProvider.getSeqNo());
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error("即信批次还款解除冻结可用资金异常:", e);
            }
        } else {
            log.info("=============================即信批次还款检验参数回调===========================");
            log.info("回调成功!");
            try {
                //更新批次状态
                thirdBatchLogBiz.updateBatchLogState(repayCheckResp.getBatchNo(), NumberHelper.toLong(acqResMap.get("repaymentId")), 1);
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
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("repaymentId")),
                        MqConfig.ACQ_RES, repayRunResp.getAcqRes(),
                        MqConfig.BATCH_NO, StringHelper.toString(repayRunResp.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayRunCall send mq exception", e);
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
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), borrowId, 2);
        } else {
            log.info("=============================即信批次放款检验参数回调===========================");
            log.info("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(lendRepayCheckResp.getBatchNo(), borrowId, 1);
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
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("borrowId")),
                        MqConfig.BATCH_NO, StringHelper.toString(lendRepayRunResp.getBatchNo()),
                        MqConfig.BATCH_RESP, GSON.toJson(lendRepayRunResp),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchLendRepayRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchLendRepayRunCall send mq exception", e);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 批次名义借款人垫付参数检查回调
     */
    public ResponseEntity<String>
    thirdBatchAdvanceCheckCall(HttpServletRequest request, HttpServletResponse response) {
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
        } else {
            log.info("=============================批次名义借款人垫付参数成功回调===========================");
            log.info("回调成功!");
            thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), repaymentId, 1);
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
        String freezeOrderId = StringHelper.toString(acqResMap.get("freezeOrderId"));//名义借款人垫付冻结订单id
        String accountId = StringHelper.toString(acqResMap.get("accountId"));//担保人账户id
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);
        String txAmount = batchBailRepayCheckResp.getTxAmount();
        UserThirdAccount titularUserThirdAccount = userThirdAccountService.findByAccountId(accountId);//担保人存管信息
        //更新批次状态
        thirdBatchLogBiz.updateBatchLogState(batchBailRepayCheckResp.getBatchNo(), repaymentId, 2);
        /*//解除存管资金冻结
        BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
        balanceUnfreezeReq.setAccountId(accountId);
        balanceUnfreezeReq.setTxAmount(txAmount);
        balanceUnfreezeReq.setChannel(ChannelContant.HTML);
        balanceUnfreezeReq.setOrderId(orderId);
        balanceUnfreezeReq.setOrgOrderId(freezeOrderId);
        BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
            log.error("===========================================================================");
            log.error("即信批次名义借款人垫付接除冻结资金失败：" + balanceUnfreezeResp.getRetMsg());
            log.error("===========================================================================");
            return true;
        }*/
        //解除本地冻结
        //立即还款冻结
        long frozenMoney = new Double(NumberHelper.toDouble(txAmount) * 100).longValue();
        AssetChange assetChange = new AssetChange();
        assetChange.setType(AssetChangeTypeEnum.unfreeze);  // 招标失败解除冻结资金
        assetChange.setUserId(titularUserThirdAccount.getUserId());
        assetChange.setMoney(frozenMoney);
        assetChange.setRemark("名义借款人垫付解除冻结可用资金");
        assetChange.setSourceId(repaymentId);
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setGroupSeqNo(assetChangeProvider.getSeqNo());
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

        // 触发批次名义借款人垫付业务处理队列
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
        }

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
     * 批次融资人还名义借款人账户垫款参数检查回调
     *
     * @param request
     * @param response
     */

    public ResponseEntity<String> thirdBatchRepayAdvanceCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayBailCheckResp batchRepayBailCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayBailCheckResp>() {
        });
        if (ObjectUtils.isEmpty(batchRepayBailCheckResp)) {
            log.error("=============================批次融资人还名义借款人账户垫款参数检查回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(batchRepayBailCheckResp.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        //更新批次状态
        Long repaymentId = NumberHelper.toLong(acqResMap.get("repaymentId"));
        if (!JixinResultContants.SUCCESS.equals(batchRepayBailCheckResp.getRetCode())) {
            log.error("=============================批次融资人还名义借款人账户垫款参数检查回调===========================");
            log.error("回调失败! msg:" + batchRepayBailCheckResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(batchRepayBailCheckResp.getBatchNo(), repaymentId, 2);
        } else {
            log.error("=============================批次融资人还名义借款人账户垫款参数检查成功===========================");
            thirdBatchLogBiz.updateBatchLogState(batchRepayBailCheckResp.getBatchNo(), repaymentId, 1);

            long userId = NumberHelper.toLong(acqResMap.get("userId"));
            UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(userId);
            String freezeOrderId = StringHelper.toString(acqResMap.get("freezeOrderId"));
            String freezeMoney = StringHelper.toString(acqResMap.get("freezeMoney"));//分

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
                log.error("批次融资人还名义借款人账户垫款解除冻结资金失败：" + balanceUnfreezeResp.getRetMsg());
                log.error("===========================================================================");
                return ResponseEntity.ok("error");
            }
            //解除本地冻结
            AssetChange assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.unfreeze);  // 招标失败解除冻结资金
            assetChange.setUserId(userId);
            assetChange.setMoney(new Double(NumberHelper.toDouble(freezeMoney) * 100).longValue());
            assetChange.setRemark("批次融资人还名义借款人账户垫款解除冻结可用资金");
            assetChange.setSourceId(repaymentId);
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getSeqNo());
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error("批次融资人还名义借款人账户垫款解除冻结可用资金异常:", e);
            }
        }

        return ResponseEntity.ok("success");
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
        }

        return ResponseEntity.ok("success");
    }

}
