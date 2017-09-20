package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestCheckCall;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestRunCall;
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvestRun;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryReq;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResp;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyRequest;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyResponse;
import com.gofobao.framework.api.model.bid_cancel.BidCancelReq;
import com.gofobao.framework.api.model.bid_cancel.BidCancelResp;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
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
    private ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    private TransferBuyLogService transferBuyLogService;
    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;
    @Value("${gofobao.javaDomain}")
    private String javaDomain;


    public ResponseEntity<VoBaseResp> createThirdTender(VoCreateThirdTenderReq voCreateThirdTenderReq) {
        Long userId = voCreateThirdTenderReq.getUserId();
        String txAmount = voCreateThirdTenderReq.getTxAmount();
        String orderId = voCreateThirdTenderReq.getOrderId();
        Preconditions.checkNotNull(orderId, "即信投标申请orderId不能为空!");
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
            log.error(String.format("进入投标撤回程序: %s", msg));
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
     * 投资人批次购买债权参数验证回调
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchCreditInvestCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchCreditInvestCheckCall batchCreditInvestCheckCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestCheckCall>() {
        });

        if (ObjectUtils.isEmpty(batchCreditInvestCheckCall)) {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("请求体为空!");
            ResponseEntity.ok("error");
        }

        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestCheckCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long transferId = NumberHelper.toLong(acqResMap.get("transferId"));
        if (!JixinResultContants.SUCCESS.equals(batchCreditInvestCheckCall.getRetCode())) {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("回调失败! msg:" + batchCreditInvestCheckCall.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestCheckCall.getBatchNo(), transferId, 2, ThirdBatchLogContants.BATCH_CREDIT_INVEST);
            ResponseEntity.ok("error");
        } else {
            log.error("=============================即信投资人批次购买债权参数验证回调===========================");
            log.error("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestCheckCall.getBatchNo(), transferId, 1, ThirdBatchLogContants.BATCH_CREDIT_INVEST);
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 投资人批次购买债权运行回调
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchCreditInvestRunCall batchCreditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });
        return dealBatchCreditInvest(batchCreditInvestRunCall);
    }

    /**
     * 处理批次购买债权转让
     *
     * @param batchCreditInvestRunCall
     * @return
     */
    public ResponseEntity<String> dealBatchCreditInvest(BatchCreditInvestRunCall batchCreditInvestRunCall) {
        Gson gson = new Gson();
        log.info(String.format("批量债权购买回调信息打印: %s", gson.toJson(batchCreditInvestRunCall)));
        Preconditions.checkNotNull(batchCreditInvestRunCall, "批量债权转让回调: 回调信息为空!");
        Preconditions.checkArgument(JixinResultContants.SUCCESS.equals(batchCreditInvestRunCall.getRetCode()),
                "批量债权转让回调: 请求失败!");
        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestRunCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        //=============================================
        // 保存第三方债权转让授权码
        //=============================================
        try {
            List<CreditInvestRun> creditInvestRunList = GSON.fromJson(batchCreditInvestRunCall.getSubPacks(), new TypeToken<List<CreditInvestRun>>() {
            }.getType());
            Preconditions.checkNotNull(creditInvestRunList, "批量债权转让回调: 查询批次详情为空");
            saveThirdTransferAuthCode(creditInvestRunList);
        } catch (JsonSyntaxException e) {
            log.error("保存第三方债权转让授权码!", e);
        }

        // 触发处理批次购买债权处理队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("transferId")), batchCreditInvestRunCall.getBatchNo(),
                    batchCreditInvestRunCall.getAcqRes(), GSON.toJson(batchCreditInvestRunCall));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }

       /* MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("transferId")),
                        MqConfig.BATCH_NO, StringHelper.toString(batchCreditInvestRunCall.getBatchNo()),
                        MqConfig.BATCH_RESP, GSON.toJson(batchCreditInvestRunCall),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchCreditInvestRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchCreditInvestRunCall send mq exception", e);
        }*/

        return ResponseEntity.ok("success");
    }

    /**
     * 保存第三债权转让授权号
     *
     * @param creditInvestRunList
     */
    public void saveThirdTransferAuthCode(List<CreditInvestRun> creditInvestRunList) {
        List<String> orderIds = creditInvestRunList.stream().map(creditInvestRun -> creditInvestRun.getOrderId()).collect(Collectors.toList());
        Specification<TransferBuyLog> tbls = Specifications
                .<TransferBuyLog>and()
                .eq("thirdTransferOrderId", orderIds.toArray())
                .build();
        List<TransferBuyLog> transferBuyLogList = transferBuyLogService.findList(tbls);
        Map<String, TransferBuyLog> transferBuyLogMaps = transferBuyLogList.stream().
                collect(Collectors.toMap(TransferBuyLog::getThirdTransferOrderId, Function.identity()));
        creditInvestRunList.forEach(creditInvestRun -> {
            String orderId = creditInvestRun.getOrderId();
            TransferBuyLog transferBuyLog = transferBuyLogMaps.get(orderId);
            transferBuyLog.setTransferAuthCode(creditInvestRun.getAuthCode());
        });
        transferBuyLogService.save(transferBuyLogList);
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

        /* 添加取消投标前置查询 */
        BidApplyQueryReq bidApplyQueryReq = new BidApplyQueryReq();
        bidApplyQueryReq.setAccountId(tenderUserThirdAccount.getAccountId());
        bidApplyQueryReq.setChannel(ChannelContant.HTML);
        bidApplyQueryReq.setOrgOrderId(tender.getThirdTenderOrderId());
        BidApplyQueryResp bidApplyQueryResp = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, bidApplyQueryReq, BidApplyQueryResp.class);
        /* 取消投标orderId */
        String orderId = JixinHelper.getOrderId(JixinHelper.TENDER_CANCEL_PREFIX);
        if (!JixinResultContants.SUCCESS.equals(bidApplyQueryResp.getRetCode())) {
            String msg = ObjectUtils.isEmpty(bidApplyQueryResp) ? "当前网络不稳定，请稍候重试" : bidApplyQueryResp.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        } else if ("2".equals(bidApplyQueryResp.getState()) || "4".equals(bidApplyQueryResp.getState())) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, String.format("投标还款中不能取消借款 tenderId:%s", tenderId)));
        } else if (!"9".equals(bidApplyQueryResp.getState())) {

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
        BatchCreditInvestRunCall batchCreditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });

        if (ObjectUtils.isEmpty(batchCreditInvestRunCall)) {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("请求体为空!");
        }

        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestRunCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);
        Long borrowId = NumberHelper.toLong(acqResMap.get("borrowId"));
        if (!JixinResultContants.SUCCESS.equals(batchCreditInvestRunCall.getRetCode())) {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("回调失败! msg:" + batchCreditInvestRunCall.getRetMsg());
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestRunCall.getBatchNo(), borrowId, 2, ThirdBatchLogContants.BATCH_CREDIT_END);
        } else {
            log.error("=============================投资人批次结束债权参数验证回调===========================");
            log.error("回调成功!");
            //更新批次状态
            thirdBatchLogBiz.updateBatchLogState(batchCreditInvestRunCall.getBatchNo(), borrowId, 1, ThirdBatchLogContants.BATCH_CREDIT_END);
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
    public ResponseEntity<String> thirdBatchCreditEndRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BatchCreditInvestRunCall batchCreditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });
        Map<String, Object> acqResMap = GSON.fromJson(batchCreditInvestRunCall.getAcqRes(), TypeTokenContants.MAP_TOKEN);

        if (ObjectUtils.isEmpty(batchCreditInvestRunCall)) {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("请求体为空!");
            log.error("======================================================================================");
            log.error("======================================================================================");
        }

        if (!JixinResultContants.SUCCESS.equals(batchCreditInvestRunCall.getRetCode())) {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("回调失败! msg:" + batchCreditInvestRunCall.getRetMsg());
            log.error("======================================================================================");
            log.error("======================================================================================");
        } else {
            log.error("======================================运行回调========================================");
            log.error("=============================投资人批次结束债权业务运行回调===========================");
            log.error("回调成功!");
            log.error("======================================================================================");
            log.error("======================================================================================");
        }

        //触发处理批次放款处理结果队列
        try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(acqResMap.get("borrowId")), batchCreditInvestRunCall.getBatchNo(),
                    batchCreditInvestRunCall.getAcqRes(), GSON.toJson(batchCreditInvestRunCall));
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }

/*
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(acqResMap.get("borrowId")),
                        MqConfig.BATCH_NO, StringHelper.toString(batchCreditInvestRunCall.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchCreditEndRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchCreditEndRunCall send mq exception", e);
        }
*/

        return ResponseEntity.ok("success");
    }
}
