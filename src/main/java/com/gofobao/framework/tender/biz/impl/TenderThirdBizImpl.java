package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_credit_invest.*;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyRequest;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyResponse;
import com.gofobao.framework.api.model.bid_cancel.BidCancelReq;
import com.gofobao.framework.api.model.bid_cancel.BidCancelResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoThirdBatchCreditInvest;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/6/1.
 */
@Service
@Slf4j
public class TenderThirdBizImpl implements TenderThirdBiz {

    final Gson GSON = new GsonBuilder().create();

    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    private MqHelper mqHelper;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    public ResponseEntity<VoBaseResp> createThirdTender(VoCreateThirdTenderReq voCreateThirdTenderReq) {
        Long userId = voCreateThirdTenderReq.getUserId();
        String txAmount = voCreateThirdTenderReq.getTxAmount();

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "投标人未开户!");

        String autoTenderOrderId = userThirdAccount.getAutoTenderOrderId();
        if (StringUtils.isEmpty(autoTenderOrderId)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标人未进行投标签约!"));
        }
        Long autoTenderTxAmount = userThirdAccount.getAutoTenderTxAmount();
        if (autoTenderTxAmount < (NumberHelper.toDouble(txAmount) * 100)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标金额超出签约单笔最大投标额!"));
        }
        Long autoTenderTotAmount = userThirdAccount.getAutoTenderTotAmount();
        if (autoTenderTotAmount < (NumberHelper.toDouble(txAmount) * 100)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标金额超出签约总投标额!"));
        }

        String orderId = JixinHelper.getOrderId(JixinHelper.TENDER_PREFIX);

        BidAutoApplyRequest request = new BidAutoApplyRequest();
        request.setAccountId(userThirdAccount.getAccountId());
        request.setOrderId(orderId);
        request.setTxAmount(voCreateThirdTenderReq.getTxAmount());
        request.setProductId(voCreateThirdTenderReq.getProductId());
        request.setFrzFlag(voCreateThirdTenderReq.getFrzFlag());
        request.setContOrderId(autoTenderOrderId);
        request.setAcqRes(voCreateThirdTenderReq.getAcqRes());
        request.setChannel(ChannelContant.HTML);

        BidAutoApplyResponse response = jixinManager.send(JixinTxCodeEnum.BID_AUTO_APPLY, request, BidAutoApplyResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        //更新tender记录
        Tender updTender = tenderService.findById(NumberHelper.toLong(request.getAcqRes()));
        updTender.setAuthCode(response.getAuthCode());
        updTender.setTUserId(userThirdAccount.getId());
        updTender.setThirdTenderOrderId(orderId);
        tenderService.updateById(updTender);
        return ResponseEntity.ok(VoBaseResp.ok("创建投标成功!"));
    }


    /**
     * 投资人批次购买债权
     *
     * @param voThirdBatchCreditInvest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> thirdBatchCreditInvest(VoThirdBatchCreditInvest voThirdBatchCreditInvest) throws Exception {
        Date nowDate = new Date();
        Borrow borrow = borrowService.findById(voThirdBatchCreditInvest.getBorrowId());
        Preconditions.checkNotNull(borrow, "批量债权转让: 债权装标的信息为空");
        if (!borrow.isTransfer()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "borrow非转让标!"));
        }

        //查询当前借款的所有 状态为1的 tender记录
        Specification<Tender> ts = Specifications.<Tender>and()
                .eq("borrowId", voThirdBatchCreditInvest.getBorrowId())
                .eq("status", 1)
                .build();
        List<Tender> tenderList = tenderService.findList(ts);
        Preconditions.checkNotNull(tenderList, "批量债权转让: 标的信息为空!");

        Tender oldTender = tenderService.findById(borrow.getTenderId()); // 债权转让原tender
        Preconditions.checkNotNull(oldTender, "批量债权转让: 债权原始投标记录为空!");
        Borrow oldBorrow = borrowService.findById(oldTender.getBorrowId());
        Preconditions.checkNotNull(oldBorrow, "批量债权转让: 债权原始投标信息为空!");

        double transferFeeRate = Math.min(0.004 + 0.0008 * (borrow.getTotalOrder() - 1), 0.0128); // 获取债权转让费用
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());
        ResponseEntity<VoBaseResp> thirdAccountConditionResponse = ThirdAccountHelper.conditionCheck(borrowUserThirdAccount);
        if(!thirdAccountConditionResponse.getStatusCode().equals(HttpStatus.OK)){
            return thirdAccountConditionResponse ;
        }

        List<CreditInvest> creditInvestList = new ArrayList<>() ;
        CreditInvest creditInvest = null;
        UserThirdAccount tenderUserThirdAccount = null;
        int sumCount = 0;
        double validMoney = 0;
        double txFee = 0;
        double transferFee = borrow.getMoney() * transferFeeRate;  // 转让管理费
        for (Tender tender : tenderList) {
            txFee = 0;
            if (!ObjectUtils.isEmpty(tender.getThirdTransferFlag()) && tender.getThirdTransferFlag()) {  //判断标的是否已在存管转让
                continue;
            }

            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            ResponseEntity<VoBaseResp> tenderUserThirdAccountConditionResponse = ThirdAccountHelper.conditionCheck(tenderUserThirdAccount);
            if(!tenderUserThirdAccountConditionResponse.getStatusCode().equals(HttpStatus.OK)){
                return thirdAccountConditionResponse ;
            }

            validMoney = tender.getValidMoney();  //投标有效金额
            sumCount += validMoney;
            txFee += (int) MathHelper.myRound((validMoney /  new Double(borrow.getMoney())) * transferFee, 0);
            String transferOrderId = JixinHelper.getOrderId(JixinHelper.LEND_REPAY_PREFIX);
            creditInvest = new CreditInvest();
            creditInvest.setAccountId(tenderUserThirdAccount.getAccountId());
            creditInvest.setOrderId(transferOrderId);
            creditInvest.setTxAmount(StringHelper.formatDouble(validMoney, 100, false));
            creditInvest.setTxFee(StringHelper.formatDouble(txFee, 100, false));
            creditInvest.setTsfAmount(StringHelper.formatDouble(validMoney, 100, false));
            creditInvest.setForAccountId(borrowUserThirdAccount.getAccountId());
            creditInvest.setOrgOrderId(oldTender.getThirdTenderOrderId());
            creditInvest.setOrgTxAmount(StringHelper.formatDouble(oldTender.getValidMoney(), 100, false));
            creditInvest.setProductId(oldBorrow.getProductId());
            creditInvest.setContOrderId(tenderUserThirdAccount.getAutoTransferBondOrderId());
            creditInvestList.add(creditInvest);
            tender.setThirdTransferOrderId(transferOrderId);
            tenderService.updateById(tender);
        }

        //批次号
        String batchNo = jixinHelper.getBatchNo();
        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("borrowId", voThirdBatchCreditInvest.getBorrowId());
        //调用存管批次债权转让接口
        BatchCreditInvestReq request = new BatchCreditInvestReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(sumCount, 100, false));
        request.setTxCounts(StringHelper.toString(creditInvestList.size()));
        request.setSubPacks(GSON.toJson(creditInvestList));
        request.setAcqRes(GSON.toJson(acqResMap));
        request.setChannel(ChannelContant.HTML);
        request.setNotifyURL(javaDomain + "/pub/tender/v2/third/batch/creditinvest/check");
        request.setRetNotifyURL(javaDomain + "/pub/tender/v2/third/batch/creditinvest/run");
        BatchCreditInvestResp response = jixinManager.send(JixinTxCodeEnum.BATCH_CREDIT_INVEST, request, BatchCreditInvestResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            throw new Exception("投资人批次购买债权失败!:" + response.getRetMsg());
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(voThirdBatchCreditInvest.getBorrowId());
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_CREDIT_INVEST);
        thirdBatchLog.setRemark("投资人批次购买债权");
        thirdBatchLogService.save(thirdBatchLog);
        return null;
    }


    /**
     * 投资人批次购买债权参数验证回调
     *
     * @return
     */
    public void thirdBatchCreditInvestCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchCreditInvestRunCall lendRepayRunResp = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });

        if (ObjectUtils.isEmpty(lendRepayRunResp)) {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode())) {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("回调失败! msg:" + lendRepayRunResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(lendRepayRunResp.getBatchNo(), NumberHelper.toLong(lendRepayRunResp.getAcqRes()), 2);
        } else {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(lendRepayRunResp.getBatchNo(), NumberHelper.toLong(lendRepayRunResp.getAcqRes()), 1);
        }

        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 投资人批次购买债权运行回调
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchCreditInvestRunCall creditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });
        Gson gson = new Gson() ;
        log.info(String.format("批量债权购买回调信息打印: %s", gson.toJson(creditInvestRunCall))) ;
        Preconditions.checkNotNull(creditInvestRunCall, "批量债权转让回调: 回调信息为空!") ;
        Preconditions.checkArgument(JixinResultContants.SUCCESS.equals(creditInvestRunCall.getRetCode()),
                "批量债权转让回调: 请求失败!");
        Map<String, Object> acqResMap = GSON.fromJson(creditInvestRunCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        //=============================================
        // 保存第三方债权转让授权码
        //=============================================
        List<CreditInvestRun> creditInvestRunList = GSON.fromJson(creditInvestRunCall.getSubPacks(), new TypeToken<List<CreditInvestRun>>() {
        }.getType());
        Preconditions.checkNotNull(creditInvestRunList, "批量债权转让回调: 查询批次详情为空") ;
        saveThirdTransferAuthCode(creditInvestRunList);


        // 触发处理批次购买债权处理队列
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("borrowId")),
                        MqConfig.BATCH_NO, StringHelper.toString(creditInvestRunCall.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchCreditInvestRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchCreditInvestRunCall send mq exception", e);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 保存第三债权转让授权号
     *
     * @param creditInvestRunList
     */
    public void saveThirdTransferAuthCode(List<CreditInvestRun> creditInvestRunList) {
        List<String> orderIds = creditInvestRunList.stream().map(creditInvestRun -> creditInvestRun.getOrderId()).collect(Collectors.toList());
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .in("thirdTransferOrderId", orderIds.toArray())
                .build();

        List<Tender> tenderList = tenderService.findList(ts) ;
        Map<String, Tender> tenderMap = tenderList.stream().collect(Collectors.toMap(Tender::getThirdTenderOrderId, Function.identity()));
        creditInvestRunList.forEach(creditInvestRun -> {
            String orderId = creditInvestRun.getOrderId();
            Tender tender = tenderMap.get(orderId);
            tender.setTransferAuthCode(creditInvestRun.getAuthCode());
        });

        tenderService.save(tenderList);
    }

    /**
     * 取消即信投标申请
     *
     * @param voCancelThirdTenderReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> cancelThirdTender(VoCancelThirdTenderReq voCancelThirdTenderReq) {
        Long tenderId = voCancelThirdTenderReq.getTenderId();
        Preconditions.checkNotNull(tenderId, "tenderThirdBizImpl cancelThirdTender: tenderId为空!");
        Tender tender = tenderService.findById(tenderId);
        Preconditions.checkNotNull(tender, "tenderThirdBizImpl cancelThirdTender: tender为空!");
        UserThirdAccount tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
        Preconditions.checkNotNull(tenderUserThirdAccount, "tenderThirdBizImpl cancelThirdTender: 投资人未开户!");
        Borrow borrow = borrowService.findById(tender.getBorrowId());
        Preconditions.checkNotNull(borrow, "tenderThirdBizImpl cancelThirdTender: tender为空!");

        String orderId = JixinHelper.getOrderId(JixinHelper.TENDER_CANCEL_PREFIX);
        BidCancelReq request = new BidCancelReq();
        request.setAccountId(tenderUserThirdAccount.getAccountId());
        request.setTxAmount(StringHelper.formatDouble(tender.getValidMoney(), 100, false));
        request.setChannel(ChannelContant.HTML);
        request.setOrderId(orderId);
        request.setOrgOrderId(tender.getThirdTenderOrderId());
        request.setProductId(borrow.getProductId());
        request.setAcqRes(StringHelper.toString(tenderId));
        BidCancelResp response = jixinManager.send(JixinTxCodeEnum.BID_CANCEL, request, BidCancelResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        tender.setThirdTenderCancelOrderId(orderId);
        tenderService.save(tender);
        return ResponseEntity.ok(VoBaseResp.ok("取消成功!"));
    }


    /**
     * 投资人批次结束债权参数验证回调
     *
     * @return
     */
    public void thirdBatchCreditEndCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchCreditInvestRunCall lendRepayRunResp = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });

        if (ObjectUtils.isEmpty(lendRepayRunResp)) {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode())) {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("回调失败! msg:" + lendRepayRunResp.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(lendRepayRunResp.getBatchNo(), NumberHelper.toLong(lendRepayRunResp.getAcqRes()), 2);
        } else {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(lendRepayRunResp.getBatchNo(), NumberHelper.toLong(lendRepayRunResp.getAcqRes()), 1);
        }

        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 投资人批次结束债权业务运行回调
     *
     * @return
     */
    public void thirdBatchCreditEndRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchCreditInvestRunCall creditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });

        if (ObjectUtils.isEmpty(creditInvestRunCall)) {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("请求体为空!");
            log.error("======================================================================================");
            log.error("======================================================================================");
        }

        if (!JixinResultContants.SUCCESS.equals(creditInvestRunCall.getRetCode())) {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("回调失败! msg:" + creditInvestRunCall.getRetMsg());
            log.error("======================================================================================");
            log.error("======================================================================================");
        }else {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("回调成功!");
            log.error("======================================================================================");
            log.error("======================================================================================");
        }

        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
