package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_credit_invest.*;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyRequest;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyResponse;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.contants.ThirdBatchNoTypeContant;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoThirdBatchCreditInvest;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    private TenderBiz tenderBiz;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Value("${gofobao.webDomain}")
    private String webDomain;

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
        return null;
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
        Long borrowId = voThirdBatchCreditInvest.getBorrowId();
        if (ObjectUtils.isEmpty(borrowId)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "borrowId不存在!"));
        }

        Borrow borrow = borrowService.findById(borrowId);
        if (!borrow.isTransfer()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "borrow非转让标!"));
        }

        //查询当前借款的所有 状态为1的 tender记录
        Specification<Tender> ts = Specifications.<Tender>and()
                .eq("borrowId", borrowId)
                .eq("status", 1)
                .build();
        List<Tender> tenderList = tenderService.findList(ts);
        if (CollectionUtils.isEmpty(tenderList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "不存在有效的投标记录!"));
        }

        Tender oldTender = tenderService.findById(borrow.getTenderId());//债权转让原tender
        if (ObjectUtils.isEmpty(oldTender)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "要转让的投标记录不存在!"));
        }

        Borrow oldBorrow = borrowService.findById(oldTender.getBorrowId());
        if (ObjectUtils.isEmpty(oldBorrow)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "要转让的投标的借款记录不存在!"));
        }


        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        List<CreditInvest> creditInvestList = new ArrayList<>();
        CreditInvest creditInvest = null;
        UserThirdAccount tenderUserThirdAccount = null;
        int sumCount = 0;
        int validMoney = 0;
        String transferOrderId = JixinHelper.getOrderId(JixinHelper.LEND_REPAY_PREFIX);
        for (Tender tender : tenderList) {
            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            validMoney = tender.getValidMoney();
            sumCount += validMoney;

            creditInvest = new CreditInvest();
            creditInvest.setAccountId(tenderUserThirdAccount.getAccountId());
            creditInvest.setOrderId(transferOrderId);
            creditInvest.setTxAmount(StringHelper.formatDouble(validMoney, 100, false));
            creditInvest.setTxFee("0");
            creditInvest.setTsfAmount(StringHelper.formatDouble(borrow.getMoney(), 100, false));
            creditInvest.setForAccountId(borrowUserThirdAccount.getAccountId());
            creditInvest.setOrgOrderId(oldTender.getThirdTenderOrderId());
            creditInvest.setOrgTxAmount(StringHelper.formatDouble(oldTender.getValidMoney(), 100, false));
            creditInvest.setProductId(oldBorrow.getProductId());
            creditInvest.setContOrderId(tenderUserThirdAccount.getAutoTransferBondOrderId());
            creditInvestList.add(creditInvest);

            tender.setThirdTransferOrderId(transferOrderId);
            tenderService.updateById(tender);
        }

        //记录日志
        String batchNo = jixinHelper.getBatchNo();
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchNoTypeContant.BATCH_CREDIT_INVEST);
        thirdBatchLog.setRemark("投资人批次购买债权");
        thirdBatchLogService.save(thirdBatchLog);

        BatchCreditInvestReq request = new BatchCreditInvestReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(sumCount, 100, false));
        request.setTxCounts(StringHelper.toString(creditInvestList.size()));
        request.setSubPacks(GSON.toJson(creditInvestList));
        request.setAcqRes(StringHelper.toString(borrowId));
        request.setChannel(ChannelContant.HTML);
        request.setNotifyURL(webDomain + "/pub/tender/v2/third/batch/creditinvest/check");
        request.setRetNotifyURL(webDomain + "/pub/tender/v2/third/batch/creditinvest/run");
        BatchCreditInvestResp response = jixinManager.send(JixinTxCodeEnum.BATCH_CREDIT_INVEST, request, BatchCreditInvestResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {

            throw new Exception("投资人批次购买债权失败!:" + response.getRetMsg());
        }
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
        }

        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 投资人批次购买债权运行回调
     *
     * @return
     */
    public void thirdBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse response) {

        BatchCreditInvestRunCall creditInvestRunCall = jixinManager.callback(request, new TypeToken<BatchCreditInvestRunCall>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(creditInvestRunCall)) {
            log.error("=============================即信投资人批次购买债权处理结果回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(creditInvestRunCall.getRetCode())) {
            log.error("=============================即信投资人批次购买债权处理结果回调===========================");
            log.error("回调失败! msg:" + creditInvestRunCall.getRetMsg());
            bool = false;
        }

        int num = NumberHelper.toInt(creditInvestRunCall.getFailCounts());
        if (num > 0) {
            log.error("=============================即信投资人批次购买债权处理结果回调===========================");
            log.error("即信投资人批次购买债权失败! 一共:" + num + "笔");
            bool = false;
        }

        if (bool) {
            long borrowId = NumberHelper.toLong(creditInvestRunCall.getAcqRes());
            Borrow borrow = borrowService.findById(borrowId);

            List<CreditInvestRun> creditInvestRunList = GSON.fromJson(creditInvestRunCall.getSubPacks(), new TypeToken<CreditInvestRun>() {
            }.getType());

            //保存第三债权转让订单号
            saveThirdTransferOrderId(creditInvestRunList);

            try {
                bool = borrowBiz.transferedBorrowAgainVerify(borrow);
            } catch (Exception e) {
                log.error("非流转标复审异常:", e);
            }
        }
        if (bool) {
            log.info("非流转标复审成功!");
        } else {
            log.info("非流转标复审失败!");
        }

        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存第三债权转让订单号
     *
     * @param creditInvestRunList
     */
    public void saveThirdTransferOrderId(List<CreditInvestRun> creditInvestRunList) {
        StringBuffer orderIds = new StringBuffer();

        Optional<List<CreditInvestRun>> creditInvestRunOption = Optional.ofNullable(creditInvestRunList);
        creditInvestRunOption.ifPresent(list -> creditInvestRunList.forEach(obj -> {
            orderIds.append(obj.getOrderId()).append(",");
        }));

        Specification<Tender> ts = Specifications
                .<Tender>and()
                .in("thirdTransferOrderId", orderIds.substring(0, orderIds.length() - 1))
                .build();
        List<Tender> tenderList = tenderService.findList(ts);

        creditInvestRunOption = Optional.of(creditInvestRunList);
        Optional<List<Tender>> tenderOptional = Optional.of(tenderList);
        creditInvestRunOption.ifPresent(list -> creditInvestRunList.forEach(creditInvestRun -> {
            tenderOptional.ifPresent(list2 -> tenderList.forEach(tender -> {
                if (creditInvestRun.getOrderId().equals(tender.getThirdTransferOrderId())) {
                    tender.setAuthCode(creditInvestRun.getAuthCode());
                }
            }));
        }));
        tenderService.save(tenderList);
    }
}
