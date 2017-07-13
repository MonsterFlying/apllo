package com.gofobao.framework.tender.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_credit_invest.*;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.batch_details_query.DetailsQueryResp;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyRequest;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyResponse;
import com.gofobao.framework.api.model.bid_cancel.BidCancelReq;
import com.gofobao.framework.api.model.bid_cancel.BidCancelResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.contants.ThirdBatchNoTypeContant;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
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
import java.io.PrintWriter;
import java.util.*;

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

        double transferFeeRate = 0;
        if ((borrow.getType() == 0) && borrow.isTransfer()) { //转让管理费
            transferFeeRate = Math.min(0.004 + 0.0008 * (borrow.getTotalOrder() - 1), 0.0128);
        }

        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());
        List<CreditInvest> creditInvestList = new ArrayList<>();
        CreditInvest creditInvest = null;
        UserThirdAccount tenderUserThirdAccount = null;
        int sumCount = 0;
        int validMoney = 0;
        int txFee = 0;
        for (Tender tender : tenderList) {
            txFee = 0;

            if (tender.getThirdTransferFlag()) {//判断标的是否已在存管转让
                continue;
            }

            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            validMoney = tender.getValidMoney();//投标有效金额
            sumCount += validMoney;

            if ((borrow.getType() == 0) && borrow.isTransfer()) { //转让管理费
                txFee += (int) MathHelper.myRound(validMoney / borrow.getMoney() * transferFeeRate, 0);
            }

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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 投资人批次购买债权运行回调
     *
     * @return
     */
    public void thirdBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

        //=============================================
        // 保存第三债权转让授权码
        //=============================================
        List<CreditInvestRun> creditInvestRunList = GSON.fromJson(creditInvestRunCall.getSubPacks(), new TypeToken<List<CreditInvestRun>>() {
        }.getType());
        saveThirdTransferAuthCode(creditInvestRunList);

        //========================================
        // 处理失败批次
        //========================================
        Long borrowId = NumberHelper.toLong(creditInvestRunCall.getAcqRes());//获取borrowid
        int num = NumberHelper.toInt(creditInvestRunCall.getFailCounts());
        String batchNo = creditInvestRunCall.getBatchNo();//批次号
        do {
            if (num <= 0) {
                break;
            }
            bool = false;

            log.error("=============================即信投资人批次购买债权处理结果回调===========================");
            log.error("即信投资人批次购买债权失败! 一共:" + num + "笔");

            Date nowDate = new Date();

            //0.查询gfb_third_batch_log标获取批次发送时间
            Specification<ThirdBatchLog> tbls = Specifications
                    .<ThirdBatchLog>and()
                    .eq("sourceId", borrowId)
                    .eq("batchNo", batchNo)
                    .build();
            List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
            if (CollectionUtils.isEmpty(thirdBatchLogList)) {
                log.error("债权转让回撤：thirdBatchLog记录不存在！");
                break;
            }

            //1.查询批次交易明细
            BatchDetailsQueryReq batchDetailsQueryReq = new BatchDetailsQueryReq();
            batchDetailsQueryReq.setBatchNo(creditInvestRunCall.getBatchNo());
            batchDetailsQueryReq.setBatchTxDate(DateHelper.dateToString(thirdBatchLogList.get(0).getCreateAt(), DateHelper.DATE_FORMAT_YMD_NUM));
            batchDetailsQueryReq.setType("2");
            batchDetailsQueryReq.setPageNum("1");
            batchDetailsQueryReq.setPageSize("10");
            batchDetailsQueryReq.setChannel(ChannelContant.HTML);
            BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, batchDetailsQueryReq, BatchDetailsQueryResp.class);
            if ((ObjectUtils.isEmpty(batchDetailsQueryResp)) || (!JixinResultContants.SUCCESS.equals(batchDetailsQueryResp.getRetCode()))) {
                log.error(ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : batchDetailsQueryResp.getRetMsg());
                break;
            }

            //2.筛选失败批次
            List<String> failureThirdTransferOrderIds = new ArrayList<>(); //转让标orderId
            List<String> successThirdTransferOrderIds = new ArrayList<>(); //转让标orderId
            List<DetailsQueryResp> detailsQueryRespList = GSON.fromJson(batchDetailsQueryResp.getSubPacks(), new TypeToken<List<DetailsQueryResp>>() {
            }.getType());
            if (CollectionUtils.isEmpty(detailsQueryRespList)) {
                log.info("================================================================================");
                log.info("债权转让批次查询：查询未发现失败批次！");
                log.info("================================================================================");
            }

            Optional<List<DetailsQueryResp>> detailsQueryRespOptional = Optional.of(detailsQueryRespList);
            detailsQueryRespOptional.ifPresent(list -> detailsQueryRespList.forEach(obj -> {
                if ("F".equalsIgnoreCase(obj.getTxState())) {
                    failureThirdTransferOrderIds.add(obj.getOrderId());
                } else {
                    successThirdTransferOrderIds.add(obj.getOrderId());
                }
            }));

            if (CollectionUtils.isEmpty(failureThirdTransferOrderIds)) {
                log.info("================================================================================");
                log.info("债权转让批次查询：查询未发现失败批次！");
                log.info("================================================================================");
            }

            //失败批次对应债权
            List<Long> borrowIdList = new ArrayList<>();
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .in("thirdTransferOrderId", failureThirdTransferOrderIds.toArray())
                    .build();
            List<Tender> failureTenderList = tenderService.findList(ts);
            for (Tender tender : failureTenderList) {
                borrowIdList.add(tender.getBorrowId());
            }

            //成功批次对应债权
            ts = Specifications
                    .<Tender>and()
                    .in("thirdTransferOrderId", successThirdTransferOrderIds.toArray())
                    .build();
            List<Tender> successTenderList = tenderService.findList(ts);
            for (Tender tender : successTenderList) {
                tender.setThirdTransferFlag(true);
            }
            tenderService.save(successTenderList);

            //3.与本地失败投标做匹配，并提出tender
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIdList.toArray())
                    .build();
            List<Borrow> borrowList = borrowService.findList(bs);

            //4.本地资金进行资金解封操作
            int failAmount = 0;//失败金额
            int failNum = 0;//失败次数
            Set<Long> borrowIdSet = new HashSet<>();
            for (Borrow borrow : borrowList) {
                failAmount = 0;
                failNum = 0;
                if (!borrowIdSet.contains(borrow.getId())) {
                    for (Tender tender : successTenderList) {
                        if (StringHelper.toString(borrow.getId()).equals(StringHelper.toString(tender.getBorrowId()))) {
                            failAmount += tender.getValidMoney(); //失败金额

                            //对冻结资金进行回滚
                            tender.setId(tender.getId());
                            tender.setStatus(2); // 取消状态
                            tender.setUpdatedAt(nowDate);

                            CapitalChangeEntity entity = new CapitalChangeEntity();
                            entity.setType(CapitalChangeEnum.Unfrozen);
                            entity.setUserId(tender.getUserId());
                            entity.setMoney(tender.getValidMoney());
                            entity.setRemark("借款 [" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "] 投标与存管通信失败，解除冻结资金。");
                            try {
                                capitalChangeHelper.capitalChange(entity);
                            } catch (Throwable e) {
                                log.error("tenderThirdBizImpl thirdBatchCreditInvestRunCall error：", e);
                            }

                            //可以加上同步资金操作
                        }
                    }
                    borrow.setTenderCount(borrow.getTenderCount() - failNum);
                    borrow.setMoneyYes(borrow.getMoneyYes() - failAmount);
                }
            }
            borrowService.save(borrowList);
            tenderService.save(successTenderList);

            log.error("已对无效投标进行撤回！");
        } while (false);

        if (bool && !ObjectUtils.isEmpty(borrowId)) {
            Borrow borrow = borrowService.findById(borrowId);
            try {
                bool = borrowBiz.transferBorrowAgainVerify(borrow);
            } catch (Throwable e) {
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
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /**
     * 保存第三债权转让授权号
     *
     * @param creditInvestRunList
     */
    public void saveThirdTransferAuthCode(List<CreditInvestRun> creditInvestRunList) {
        List<String> orderIds = new ArrayList<>();

        Optional<List<CreditInvestRun>> creditInvestRunOption = Optional.ofNullable(creditInvestRunList);
        creditInvestRunOption.ifPresent(list -> creditInvestRunList.forEach(obj -> {
            orderIds.add(obj.getOrderId());
        }));

        Specification<Tender> ts = Specifications
                .<Tender>and()
                .in("thirdTransferOrderId", orderIds.toArray())
                .build();
        List<Tender> tenderList = tenderService.findList(ts);

        creditInvestRunOption = Optional.of(creditInvestRunList);
        Optional<List<Tender>> tenderOptional = Optional.of(tenderList);
        creditInvestRunOption.ifPresent(list -> creditInvestRunList.forEach(creditInvestRun -> {
            tenderOptional.ifPresent(list2 -> tenderList.forEach(tender -> {
                if (creditInvestRun.getOrderId().equals(tender.getThirdTransferOrderId())) {
                    tender.setTransferAuthCode(creditInvestRun.getAuthCode());
                }
            }));
        }));
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
}
