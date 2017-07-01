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
import com.gofobao.framework.api.model.trustee_pay.TrusteePayReq;
import com.gofobao.framework.api.model.trustee_pay.TrusteePayResp;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchRepay;
import com.gofobao.framework.scheduler.biz.TaskSchedulerBiz;
import com.gofobao.framework.scheduler.constants.TaskSchedulerConstants;
import com.gofobao.framework.scheduler.entity.TaskScheduler;
import com.gofobao.framework.system.contants.ThirdBatchNoTypeContant;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.*;

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
    @Autowired
    private MqHelper mqHelper;

    @Autowired
    private ThirdAccountPasswordHelper thirdAccountPasswordHelper;

    @Autowired
    TaskSchedulerBiz taskSchedulerBiz;

    @Value("${gofobao.webDomain}")
    private String webDomain;
    @Value("${jixin.redPacketAccountId}")
    private String redPacketAccountId;

    /**
     * 登记即信标的
     *
     * @param voCreateThirdBorrowReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> createThirdBorrow(VoCreateThirdBorrowReq voCreateThirdBorrowReq) {
        Long borrowId = voCreateThirdBorrowReq.getBorrowId();
        boolean entrustFlag = voCreateThirdBorrowReq.getEntrustFlag();

        if (ObjectUtils.isEmpty(borrowId)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款id为空!"));
        }

        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "借款记录不存在！");

        Long userId = borrow.getUserId();
        int repayFashion = borrow.getRepayFashion();

        Long takeUserId = borrow.getTakeUserId();   // 公司实际收款人
        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");
        String productId = jixinHelper.generateProductId(borrowId);

        DebtRegisterRequest debtRegisterRequest = new DebtRegisterRequest();
        debtRegisterRequest.setAccountId(userThirdAccount.getAccountId());
        debtRegisterRequest.setProductId(productId);
        debtRegisterRequest.setProductDesc(borrow.getName());
        debtRegisterRequest.setRaiseDate(DateHelper.dateToString(borrow.getReleaseAt(), DateHelper.DATE_FORMAT_YMD_NUM));
        debtRegisterRequest.setRaiseEndDate(DateHelper.dateToString(DateHelper.addDays(DateHelper.beginOfDate(borrow.getReleaseAt()), borrow.getValidDay() + 1), DateHelper.DATE_FORMAT_YMD_NUM));
        debtRegisterRequest.setIntType(StringHelper.toString(repayFashion == 1 ? 0 : 1));
        int duration = 0;
        if (repayFashion != 1) {
            Date releaseAt = borrow.getReleaseAt();
            debtRegisterRequest.setIntPayDay(DateHelper.dateToString(releaseAt, "dd"));
            duration = DateHelper.diffInDays(DateHelper.addMonths(releaseAt, borrow.getTimeLimit()), releaseAt, false);
        } else {//一次性还本付息
            duration = borrow.getTimeLimit();
        }
        debtRegisterRequest.setDuration(StringHelper.toString(duration));
        debtRegisterRequest.setTxAmount(StringHelper.formatDouble(borrow.getMoney(), 100, false));
        debtRegisterRequest.setRate(StringHelper.formatDouble(borrow.getApr(), 100, false));
        debtRegisterRequest.setTxFee("0");
        String bailAccountId = jixinHelper.getBailAccountId(borrowId);
        debtRegisterRequest.setBailAccountId(bailAccountId);
        debtRegisterRequest.setAcqRes(StringHelper.toString(borrowId));
        debtRegisterRequest.setChannel(ChannelContant.HTML);
        if (entrustFlag && !ObjectUtils.isEmpty(takeUserThirdAccount)) {
            debtRegisterRequest.setEntrustFlag("1");
            debtRegisterRequest.setReceiptAccountId(takeUserThirdAccount.getAccountId());
        }

        DebtRegisterResponse response = jixinManager.send(JixinTxCodeEnum.DEBT_REGISTER, debtRegisterRequest, DebtRegisterResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }

        borrow.setBailAccountId(bailAccountId);
        borrow.setProductId(productId);
        borrowService.updateById(borrow);
        return null;
    }

    /**
     * 取消即信借款
     *
     * @param voCancelThirdBorrow
     * @return
     */
    public ResponseEntity<VoBaseResp> cancelThirdBorrow(VoCancelThirdBorrow voCancelThirdBorrow) {
        Long userId = voCancelThirdBorrow.getUserId();
        String productId = voCancelThirdBorrow.getProductId();
        String raiseDate = voCancelThirdBorrow.getRaiseDate();//募集日期
        if (ObjectUtils.isEmpty(raiseDate)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "募集日期不能为空!"));
        }

        if (ObjectUtils.isEmpty(productId)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "borrowId为空"));
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        DebtRegisterCancelReq request = new DebtRegisterCancelReq();
        request.setChannel(ChannelContant.HTML);
        request.setAccountId(userThirdAccount.getAccountId());
        request.setProductId(productId);
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
        String productId = voQueryThirdBorrowList.getProductId();
        String startDate = voQueryThirdBorrowList.getStartDate();
        String endDate = voQueryThirdBorrowList.getEndDate();
        String pageNum = voQueryThirdBorrowList.getPageNum();//页码 从1开始
        String pageSize = voQueryThirdBorrowList.getPageSize();

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        if (ObjectUtils.isEmpty(productId) && (StringUtils.isEmpty(startDate) || StringUtils.isEmpty(endDate)) && (StringUtils.isEmpty(pageNum) || StringUtils.isEmpty(pageSize))) {
            return debtDetailsQueryResp;
        }

        DebtDetailsQueryReq request = new DebtDetailsQueryReq();
        request.setChannel(ChannelContant.HTML);
        request.setAccountId(userThirdAccount.getAccountId());
        if (!ObjectUtils.isEmpty(productId)) {
            request.setProductId(productId);
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
        String paramStr = voRepayAllReq.getParamStr();
        if (!SecurityHelper.checkSign(voRepayAllReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }

        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long borrowId = NumberHelper.toLong(paramMap.get("borrowId"));

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
        request.setRetNotifyURL(webDomain + "/pub/borrow/v2/third/repayall/run");
        request.setNotifyURL(webDomain + "/pub/borrow/v2/third/repayall/check");
        request.setAcqRes(GSON.toJson(borrowId));
        request.setSubPacks(GSON.toJson(tempRepayList));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(tempRepayList.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "即信批次还款失败:" + response.getRetMsg()));
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
    public ResponseEntity<String> thirdBatchRepayAllCheckCall(HttpServletRequest request, HttpServletResponse response) {
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

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次还款(提前结清)
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchRepayAllRunCall(HttpServletRequest request, HttpServletResponse response) {
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
        return ResponseEntity.ok("success");
    }


    /**
     * 即信受托支付
     *
     * @param voThirdTrusteePayReq
     * @param httpServletRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoHtmlResp> thirdTrusteePay(VoThirdTrusteePayReq voThirdTrusteePayReq, HttpServletRequest httpServletRequest) {
        long borrowId = voThirdTrusteePayReq.getBorrowId();
        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "borrowThirdBizImpl thirdTrusteePay： 借款记录不存在!");

        long lendUserId = borrow.getUserId(); //借款人id
        UserThirdAccount lendUserThirdAccount = userThirdAccountService.findByUserId(lendUserId);
        Preconditions.checkNotNull(lendUserThirdAccount, "borrowThirdBizImpl thirdTrusteePay：借款人不存在!");

        long takeUserId = borrow.getTakeUserId(); //收款人id
        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
        Preconditions.checkNotNull(lendUserThirdAccount, "borrowThirdBizImpl thirdTrusteePay：收款人不存在!");

        TrusteePayReq trusteePayReq = new TrusteePayReq();
        trusteePayReq.setAccountId(lendUserThirdAccount.getAccountId());
        trusteePayReq.setChannel(ChannelContant.HTML);
        trusteePayReq.setAcqRes(StringHelper.toString(borrowId));
        trusteePayReq.setIdNo(lendUserThirdAccount.getIdNo());
        trusteePayReq.setIdType(JixinHelper.getIdType(lendUserThirdAccount.getIdType()));
        trusteePayReq.setNotifyUrl(webDomain + "/pub/borrow/v2/third/trusteepay/run");
        trusteePayReq.setForgotPwdUrl(thirdAccountPasswordHelper.getThirdAcccountResetPasswordUrl(httpServletRequest, borrow.getUserId()));   // 忘记时间
        trusteePayReq.setProductId(borrow.getProductId());
        trusteePayReq.setReceiptAccountId(takeUserThirdAccount.getAccountId());
        trusteePayReq.setRetUrl("");
        String html = jixinManager.getHtml(JixinTxCodeEnum.TRUSTEE_PAY, trusteePayReq);

        VoHtmlResp resp = VoBaseResp.ok("请求成功", VoHtmlResp.class);
        try {
            resp.setHtml(Base64Utils.encodeToString(html.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            log.error("UserThirdBizImpl autoTender gethtml exceptio", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了， 请稍候重试", VoHtmlResp.class));
        }

        TaskScheduler taskScheduler = new TaskScheduler();
        taskScheduler.setCreateAt(new Date());
        taskScheduler.setUpdateAt(new Date());
        taskScheduler.setType(TaskSchedulerConstants.TRUSTEE_PAY_QUERY);
        Map<String, String> data = new HashMap<>(1);
        data.put("borrowId", String.valueOf(borrowId));
        Gson gson = new Gson();
        taskScheduler.setTaskData(gson.toJson(data));
        taskScheduler.setTaskNum(5);
        taskScheduler = taskSchedulerBiz.save(taskScheduler);
        if (ObjectUtils.isEmpty(taskScheduler.getId())) {
            log.error(String.format("添加查询受托支付调度失败 %s", gson.toJson(data)));
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * 即信受托支付回调
     *
     * @param request
     * @param response
     */
    public ResponseEntity<String> thirdTrusteePayCall(HttpServletRequest request, HttpServletResponse response) {
        TrusteePayResp trusteePayResp = jixinManager.callback(request, new TypeToken<TrusteePayResp>() {
        });
        if (ObjectUtils.isEmpty(trusteePayResp)) {
            log.error("BorrowThirdBizImpl thirdTrusteePayCall 参数有问题");
            return ResponseEntity.ok("success");
        }

        if (!JixinResultContants.SUCCESS.equals(trusteePayResp.getRetCode())) {
            log.error("BorrowThirdBizImpl thirdTrusteePayCall 验证不通过");
            return ResponseEntity.ok("success");
        }
        Long borrowId = NumberHelper.toLong(trusteePayResp.getAcqRes());
        if (trusteePayResp.getState().equals("1")) {   // 初审通过
            //初审
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
            mqConfig.setTag(MqTagEnum.FIRST_VERIFY);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            boolean mqState = false;
            try {
                log.info(String.format("borrowThirdBizImpl firstVerify send mq %s", GSON.toJson(body)));
                mqState = mqHelper.convertAndSend(mqConfig);
            } catch (Exception e) {
                log.error("borrowThirdBizImpl firstVerify send mq exception", e);
            }

        } else { // 等待查询
            TaskScheduler taskScheduler = new TaskScheduler();
            taskScheduler.setCreateAt(new Date());
            taskScheduler.setUpdateAt(new Date());
            taskScheduler.setType(TaskSchedulerConstants.TRUSTEE_PAY_QUERY);
            Map<String, String> data = new HashMap<>(1);
            data.put("borrowId", borrowId.toString());
            Gson gson = new Gson();
            taskScheduler.setTaskData(gson.toJson(data));
            taskScheduler.setTaskNum(Integer.MAX_VALUE - 2);
            taskScheduler = taskSchedulerBiz.save(taskScheduler);
            if (ObjectUtils.isEmpty(taskScheduler.getId())) {
                log.error(String.format("添加查询受托支付调度失败 %s", gson.toJson(data)));
            }

        }

        return ResponseEntity.ok("success");
    }
}
