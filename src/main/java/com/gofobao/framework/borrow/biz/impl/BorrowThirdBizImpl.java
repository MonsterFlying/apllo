package com.gofobao.framework.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_repay.*;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryReq;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResp;
import com.gofobao.framework.api.model.debt_register.DebtRegisterRequest;
import com.gofobao.framework.api.model.debt_register.DebtRegisterResponse;
import com.gofobao.framework.api.model.debt_register_cancel.DebtRegisterCancelReq;
import com.gofobao.framework.api.model.debt_register_cancel.DebtRegisterCancelResp;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchRepay;
import com.gofobao.framework.system.contants.ThirdBatchNoTypeContant;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/6/1.
 */
@Service
@Slf4j
public class BorrowThirdBizImpl implements BorrowThirdBiz {

    final Gson GSON = new GsonBuilder().create();

    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private BorrowBiz borrowBiz;

    @Value("gofobao.webDomain")
    private String webDomain;
    @Value("jixin.redPacketAccountId")
    private String redPacketAccountId;


    public ResponseEntity<VoBaseResp> createThirdBorrow(VoCreateThirdBorrowReq voCreateThirdBorrowReq) {
        Long borrowId = voCreateThirdBorrowReq.getBorrowId();

        if (ObjectUtils.isEmpty(borrowId)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款id为空!"));
        }

        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "借款记录不存在！");

        Long userId = borrow.getUserId();
        int repayFashion = borrow.getRepayFashion();

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        DebtRegisterRequest request = new DebtRegisterRequest();
        request.setAccountId(userThirdAccount.getAccountId());
        request.setProductId(StringHelper.toString(borrowId));
        request.setProductDesc(borrow.getName());
        request.setRaiseDate(DateHelper.dateToString(borrow.getReleaseAt(), DateHelper.DATE_FORMAT_YMD_NUM));
        request.setRaiseEndDate(DateHelper.dateToString(DateHelper.addDays(borrow.getReleaseAt(), borrow.getValidDay()), DateHelper.DATE_FORMAT_YMD_NUM));
        request.setIntType(StringHelper.toString(repayFashion == 1 ? 0 : 1));
        int duration = 0;
        if (repayFashion != 1) {
            Date successAt = borrow.getSuccessAt();
            request.setIntPayDay(DateHelper.dateToString(successAt, "dd"));
            duration = DateHelper.diffInDays(DateHelper.addMonths(successAt, borrow.getTimeLimit()), successAt, false);
        } else {//一次性还本付息
            duration = borrow.getTimeLimit();
        }
        request.setDuration(StringHelper.toString(duration));
        request.setTxAmount(StringHelper.formatDouble(borrow.getMoney(), 100, false));
        request.setRate(StringHelper.formatDouble(borrow.getApr(), 100, false));
        /**
         * @// TODO: 2017/6/12 借款手续费
         */
        request.setTxFee("0");
        String bailAccountId = borrow.getBailAccountId();
        if (!ObjectUtils.isEmpty(bailAccountId)) {
            request.setBailAccountId(bailAccountId);
        }
        request.setAcqRes(StringHelper.toString(borrowId));
        request.setChannel(ChannelContant.HTML);

        DebtRegisterResponse response = jixinManager.send(JixinTxCodeEnum.DEBT_REGISTER, request, DebtRegisterResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }
        return null;
    }

    public ResponseEntity<VoBaseResp> cancelThirdBorrow(VoCancelThirdBorrow voCancelThirdBorrow) {
        Long userId = voCancelThirdBorrow.getUserId();
        Long borrowId = voCancelThirdBorrow.getBorrowId();
        String raiseDate = voCancelThirdBorrow.getRaiseDate();//募集日期
        if (ObjectUtils.isEmpty(raiseDate)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "募集日期不能为空!"));
        }

        if (ObjectUtils.isEmpty(borrowId)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "borrowId为空"));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        DebtRegisterCancelReq request = new DebtRegisterCancelReq();
        request.setChannel(ChannelContant.HTML);
        request.setAccountId(userThirdAccount.getAccountId());
        request.setProductId(StringHelper.toString(borrowId));
        request.setRaiseDate(raiseDate);

        DebtRegisterCancelResp response = jixinManager.send(JixinTxCodeEnum.DEBT_REGISTER_CANCEL, request, DebtRegisterCancelResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        return null;
    }

    public DebtDetailsQueryResp queryThirdBorrowList(VoQueryThirdBorrowList voQueryThirdBorrowList) {
        DebtDetailsQueryResp debtDetailsQueryResp = null;

        Long userId = voQueryThirdBorrowList.getUserId();
        Long borrowId = voQueryThirdBorrowList.getBorrowId();
        String startDate = voQueryThirdBorrowList.getStartDate();
        String endDate = voQueryThirdBorrowList.getEndDate();
        String pageNum = voQueryThirdBorrowList.getPageNum();//页码 从1开始
        String pageSize = voQueryThirdBorrowList.getPageSize();

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        if (ObjectUtils.isEmpty(borrowId) && (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) && (StringUtils.isEmpty(pageNum) || StringUtils.isEmpty(pageSize))) {
            return debtDetailsQueryResp;
        }

        DebtDetailsQueryReq request = new DebtDetailsQueryReq();
        request.setChannel(ChannelContant.HTML);
        request.setAccountId(userThirdAccount.getAccountId());
        if (!ObjectUtils.isEmpty(borrowId)) {
            request.setProductId(StringHelper.toString(borrowId));
        }
        if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
            request.setEndDate(endDate);
            request.setStartDate(startDate);

        }
        if (!StringUtils.isEmpty(pageNum) && !StringUtils.isEmpty(pageSize)) {
            request.setPageNum(pageNum);
            request.setPageSize(pageSize);
        }

        DebtDetailsQueryResp response = jixinManager.send(JixinTxCodeEnum.DEBT_DETAILS_QUERY, request, DebtDetailsQueryResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            log.error(msg);
        }

        return response;
    }

    /**
     * 即信批次还款(提前结清)
     *
     * @param voRepayAllReq
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdBatchRepayAll(VoRepayAllReq voRepayAllReq) {

        Date nowDate = new Date();
        Long borrowId = voRepayAllReq.getBorrowId();
        Borrow borrow = borrowService.findByIdLock(borrowId);
        Asset borrowAsset = assetService.findByUserId(borrow.getUserId());
        Preconditions.checkNotNull(borrowAsset, "借款人资产记录不存在!");

        int repaymentTotal = 0;
        List<VoThirdBatchRepay> voThirdBatchRepayList = new ArrayList<>();
        int penalty = 0;
        int lateInterest = 0;
        int lateDays = 0;
        int overPrincipal = 0;
        Date startAt = null;
        Date endAt = null;
        BorrowRepayment borrowRepayment = null;
        double interestPercent = 0;
        VoThirdBatchRepay tempVoThirdBatchRepay = null;
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("borrowId", borrowId)
                .build();
        List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);

        for (int i = 0; i < borrowRepaymentList.size(); i++) {
            borrowRepayment = borrowRepaymentList.get(i);
            if (borrowRepayment.getStatus() != 0) {
                continue;
            }

            if (borrowRepayment.getOrder() == 0) {
                startAt = DateHelper.beginOfDate(borrow.getSuccessAt());
            } else {
                startAt = DateHelper.beginOfDate(borrowRepaymentList.get(i - 1).getRepayAt());
            }
            endAt = DateHelper.beginOfDate(borrowRepayment.getRepayAt());

            //以结清第一期的14天利息作为违约金
            if (penalty == 0) {
                penalty = borrowRepayment.getInterest() / DateHelper.diffInDays(endAt, startAt, false) * 14;
            }

            Date nowStartDate = DateHelper.beginOfDate(new Date());
            if (nowStartDate.getTime() <= startAt.getTime()) {
                interestPercent = 0;
            } else {
                interestPercent = MathHelper.min(DateHelper.diffInDays(nowStartDate, startAt, false) / DateHelper.diffInDays(endAt, startAt, false), 1);
            }

            lateDays = DateHelper.diffInDays(nowStartDate, endAt, false);
            if (interestPercent == 1 && lateDays > 0) {
                for (int j = i; j < borrowRepaymentList.size(); j++) {
                    overPrincipal += borrowRepaymentList.get(j).getPrincipal();
                }
                lateInterest = new Double(overPrincipal * 0.004 * lateDays).intValue();
            }
            repaymentTotal += borrowRepayment.getPrincipal() + borrowRepayment.getInterest() * interestPercent + lateInterest;
            tempVoThirdBatchRepay = new VoThirdBatchRepay();
            tempVoThirdBatchRepay.setInterestPercent(interestPercent);
            tempVoThirdBatchRepay.setRepaymentId(borrowRepayment.getId());
            tempVoThirdBatchRepay.setUserId(borrowRepayment.getUserId());
            tempVoThirdBatchRepay.setIsUserOpen(false);
            voThirdBatchRepayList.add(tempVoThirdBatchRepay);
        }

        int repayMoney = repaymentTotal; //repaymentTotal + penalty;  官标由平台托管借款  目前实现不收取违约金
        if (borrowAsset.getUseMoney() < (repayMoney)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "结清总共需要还款 " + repayMoney + " 元，您的账户余额不足，请先充值!！"));
        }

        List<Repay> tempRepayList = new ArrayList<>(); //往后每期未还回款集合
        for (VoThirdBatchRepay tempVoRepayReq : voThirdBatchRepayList) {
            try {
                tempRepayList.addAll(borrowRepaymentThirdBiz.getRepayList(tempVoRepayReq));
            } catch (Exception e) {
                log.error("提前结清异常：", e);
            }
        }


        if (CollectionUtils.isEmpty(tempRepayList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "还款不存在"));
        }

        double sumTxAmount = 0;
        for (Repay tempRepay : tempRepayList) {
            sumTxAmount += NumberHelper.toDouble(tempRepay.getTxAmount());
        }

        //记录日志
        String batchNo = jixinHelper.getBatchNo();
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowRepayment.getId());
        thirdBatchLog.setType(ThirdBatchNoTypeContant.BATCH_REPAY);
        thirdBatchLog.setRemark("即信批次还款");
        thirdBatchLogService.save(thirdBatchLog);

        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(sumTxAmount, false));
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repay/run");
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repay/check");
        request.setAcqRes(GSON.toJson(borrowId));
        request.setSubPacks(GSON.toJson(tempRepayList));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(tempRepayList.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "即信批次还款失败!"));
        }

        //存在违约金的时候将违约金发放给投资者
        if (penalty > 0) {
            try {
                receivedPenalty(borrow, penalty);
            } catch (Exception e) {
                log.error("borrowThirdBizImpl thirdBatchRepayAll 发放违约金失败");
            }
        }

        return null;
    }

    /**
     * 提前结清给投资者违约金
     *
     * @param borrow
     * @param penalty
     */
    private void receivedPenalty(Borrow borrow, int penalty) throws Exception {
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .eq("status", 1)
                .build();
        Pageable pageable = null;
        List<Tender> tenderList = null;
        int pageNum = 0;
        int pageSize = 10;
        int tempPenalty = 0;
        long tenderUserId = 0;
        UserThirdAccount tenderUserThirdAccount = null;//投标人银汉存管账户
        do {
            pageable = new PageRequest(pageNum++, pageSize, new Sort(Sort.Direction.ASC));
            tenderList = tenderService.findList(ts, pageable);
            for (Tender tender : tenderList) {
                tenderUserId = tender.getUserId();
                tempPenalty = tender.getValidMoney() / borrow.getMoney() * penalty;
                if (tender.getTransferFlag() == 2) { //已转让
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .eq("status", 3)
                            .build();
                    List<Borrow> borrowList = borrowService.findList(bs);
                    receivedPenalty(borrowList.get(0), tempPenalty);
                    continue;
                }

                tenderUserThirdAccount = userThirdAccountService.findByUserId(tenderUserId);
                //调用即信发送红包接口
                VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                voucherPayRequest.setAccountId(redPacketAccountId);
                voucherPayRequest.setTxAmount(StringHelper.formatDouble(tempPenalty, 100, false));
                voucherPayRequest.setForAccountId(tenderUserThirdAccount.getAccountId());
                voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                voucherPayRequest.setDesLine("借款[" + borrow.getName() + "]的违约金");
                voucherPayRequest.setChannel(ChannelContant.HTML);
                VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                    String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                    throw new Exception(msg);
                }
            }
        } while (tenderList.size() < 10);
    }

    /**
     * 即信批次还款(提前结清)
     *
     * @return
     */
    public void thirdBatchRepayAllCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayCheckResp repayCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(repayCheckResp)) {
            log.error("=============================(提前结清)即信批次还款检验参数回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(repayCheckResp.getRetCode())) {
            log.error("=============================(提前结清)即信批次还款检验参数回调===========================");
            log.error("回调失败! msg:" + repayCheckResp.getRetMsg());
        } else {
            log.info("=============================(提前结清)即信批次放款检验参数回调===========================");
            log.info("即信批次还款检验参数成功!");
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
     * 即信批次还款(提前结清)
     *
     * @return
     */
    public void thirdBatchRepayAllRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayRunResp repayRunResp = jixinManager.callback(request, new TypeToken<BatchRepayRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(repayRunResp)) {
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(repayRunResp.getRetCode())) {
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("回调失败! msg:" + repayRunResp.getRetMsg());
            bool = false;
        }

        int num = NumberHelper.toInt(repayRunResp.getFailCounts());
        if (num > 0) {
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("即信批次还款处理失败! 一共:" + num + "笔");
            bool = false;
        }

        if (bool) {
            ResponseEntity<VoBaseResp> resp = null;
            try {
                Long borrowId = NumberHelper.toLong(repayRunResp.getAcqRes());
                //提前结清操作
                VoRepayAllReq voRepayAllReq = new VoRepayAllReq();
                voRepayAllReq.setBorrowId(borrowId);
                borrowBiz.repayAll(voRepayAllReq);
            } catch (Exception e) {
                log.error("提前结清异常:", e);
            }
            if (ObjectUtils.isEmpty(resp)) {
                log.info("提前结清成功!");
            }
        } else {
            log.info("提前结清失败!");
        }
        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
