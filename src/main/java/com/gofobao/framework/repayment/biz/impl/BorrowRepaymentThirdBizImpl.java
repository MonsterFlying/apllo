package com.gofobao.framework.repayment.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_bail_repay.*;
import com.gofobao.framework.api.model.batch_lend_pay.*;
import com.gofobao.framework.api.model.batch_repay.*;
import com.gofobao.framework.api.model.batch_repay_bail.*;
import com.gofobao.framework.asset.entity.AdvanceLog;
import com.gofobao.framework.asset.service.AdvanceLogService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

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
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private RepaymentBiz repaymentBiz;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private AdvanceLogService advanceLogService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    /**
     * 即信批次还款
     *
     * @param voThirdBatchRepay
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdBatchRepay(VoThirdBatchRepay voThirdBatchRepay) throws Exception {
        int lateInterest = 0;//逾期利息
        Double interestPercent = voThirdBatchRepay.getInterestPercent();
        Long repaymentId = voThirdBatchRepay.getRepaymentId();
        interestPercent = interestPercent == 0 ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        int repayInterest = (int) (borrowRepayment.getInterest() * interestPercent);//还款利息
        int repayMoney = borrowRepayment.getPrincipal() + repayInterest;//还款金额
        Long borrowId = borrow.getId();//借款ID

        //逾期天数
        Date nowDateOfBegin = DateHelper.beginOfDate(new Date());
        Date repayDateOfBegin = DateHelper.beginOfDate(borrowRepayment.getRepayAt());
        int lateDays = DateHelper.diffInDays(nowDateOfBegin, repayDateOfBegin, false);
        lateDays = lateDays < 0 ? 0 : lateDays;
        if (0 < lateDays) {
            int overPrincipal = borrowRepayment.getPrincipal();
            if (borrowRepayment.getOrder() < (borrow.getTotalOrder() - 1)) {
                Specification<BorrowRepayment> brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrowId)
                        .eq("status", 0)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                Preconditions.checkNotNull(borrowRepayment, "还款不存在");

                overPrincipal = 0;
                for (BorrowRepayment temp : borrowRepaymentList) {
                    overPrincipal += temp.getPrincipal();
                }
            }
            lateInterest = (int) MathHelper.myRound(overPrincipal * 0.004 * lateDays, 2);
        }

        List<Repay> repayList = new ArrayList<>();
        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            repayList = new ArrayList<>();
            receivedReapy(repayList, borrow, borrowUserThirdAccount.getAccountId(), borrowRepayment.getOrder(), interestPercent, lateDays, lateInterest / 2);
        } else {
            //批次融资人还担保账户垫款
            VoBatchRepayBailReq voBatchRepayBailReq = new VoBatchRepayBailReq();
            voBatchRepayBailReq.setLateInterest(lateInterest);
            voBatchRepayBailReq.setRepayMoney(repayMoney);
            voBatchRepayBailReq.setRepaymentId(repaymentId);
            thirdBatchRepayBail(voBatchRepayBailReq);
        }

        if (CollectionUtils.isEmpty(repayList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "即信存在还款项为空!"));
        }

        double txAmount = 0;
        for (Repay repay : repayList) {
            txAmount += NumberHelper.toDouble(repay.getTxAmount());
        }

        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(jixinHelper.getBatchNo());
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repay/run");
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repay/check");
        request.setAcqRes(GSON.toJson(voThirdBatchRepay));
        request.setSubPacks(GSON.toJson(repayList));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(repayList.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "即信批次还款失败!"));
        }
        return null;
    }



    /**
     * 收到垫付还款
     */

    /**
     * 收到还款
     *
     * @param borrow
     * @param order
     * @param interestPercent
     * @param borrowAccountId 借款方即信存管账户id
     * @param lateDays
     * @param lateInterest
     * @return
     * @throws Exception
     */
    private void receivedReapy(List<Repay> repayList, Borrow borrow, String borrowAccountId, int order, double interestPercent, int lateDays, int lateInterest) throws Exception {
        do {
            Repay repay = new Repay();
            int txFeeIn = 0;//投资方手续费  利息管理费
            int txAmount = 0;//融资人实际付出金额=交易金额+交易利息+还款手续费
            int intAmount = 0;//交易利息
            int txFeeOut = 0;//借款方手续费  逾期利息

            //===================================还款校验==========================================
            if (ObjectUtils.isEmpty(borrow)) {
                break;
            }

            Long borrowId = borrow.getId();
            Specification<Tender> specification = Specifications
                    .<Tender>and()
                    .eq("status", 1)
                    .eq("borrowId", borrowId)
                    .build();

            List<Tender> tenderList = tenderService.findList(specification);
            if (CollectionUtils.isEmpty(tenderList)) {
                break;
            }

            List<Long> userIds = new ArrayList<>();
            List<Long> tenderIds = new ArrayList<>();
            for (Tender tender : tenderList) {
                userIds.add(tender.getUserId());
                tenderIds.add(tender.getId());
            }

            Specification<UserCache> ucs = Specifications
                    .<UserCache>and()
                    .in("userId", userIds.toArray())
                    .build();

            List<UserCache> userCacheList = userCacheService.findList(ucs);
            if (CollectionUtils.isEmpty(userCacheList)) {
                break;
            }

            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tenderId", tenderIds.toArray())
                    .eq("status", 0)
                    .eq("order", order)
                    .build();

            List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
            if (CollectionUtils.isEmpty(borrowCollectionList)) {
                break;
            }
            //==================================================================================
            UserThirdAccount tenderUserThirdAccount = null;
            for (Tender tender : tenderList) {
                tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
                //获取当前借款的回款记录
                BorrowCollection borrowCollection = null;
                for (int i = 0; i < borrowCollectionList.size(); i++) {
                    borrowCollection = borrowCollectionList.get(i);
                    if (StringHelper.toString(tender.getId()).equals(StringHelper.toString(borrowCollection.getTenderId()))) {
                        break;
                    }
                    borrowCollection = null;
                    continue;
                }

                if (tender.getTransferFlag() == 1) {//转让中
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .in("status", 0, 1)
                            .build();

                    List<Borrow> borrowList = borrowService.findList(bs);
                    if (CollectionUtils.isEmpty(borrowList)) {
                        continue;
                    }
                }

                if (tender.getTransferFlag() == 2) { //已转让
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .eq("status", 3)
                            .build();

                    List<Borrow> borrowList = borrowService.findList(bs);
                    if (CollectionUtils.isEmpty(borrowList)) {
                        continue;
                    }

                    Borrow tempBorrow = borrowList.get(0);
                    int tempOrder = order + tempBorrow.getTotalOrder() - borrow.getTotalOrder();
                    int tempLateInterest = tender.getValidMoney() / borrow.getMoney() * lateInterest;
                    int accruedInterest = 0;
                    if (tempOrder == 0) {//如果是转让后第一期回款, 则计算转让者首期应计利息
                        int interest = borrowCollection.getInterest();
                        Date startAt = DateHelper.beginOfDate((Date) borrowCollection.getStartAt().clone());//获取00点00分00秒
                        Date collectionAt = DateHelper.beginOfDate((Date) borrowCollection.getCollectionAt().clone());
                        Date startAtYes = DateHelper.beginOfDate((Date) borrowCollection.getStartAtYes().clone());
                        Date endAt = DateHelper.beginOfDate((Date) tempBorrow.getSuccessAt().clone());

                        if (endAt.getTime() > collectionAt.getTime()) {
                            endAt = (Date) collectionAt.clone();
                        }

                        accruedInterest = Math.round(interest *
                                Math.max(DateHelper.diffInDays(startAtYes, endAt, false), 0) /
                                DateHelper.diffInDays(startAt, collectionAt, false));

                        if (accruedInterest > 0) {
                            //利息管理费
                            txFeeIn += (accruedInterest * 0.1);
                        }
                    }

                    //回调
                    receivedReapy(repayList, tempBorrow, borrowAccountId, tempOrder, interestPercent, lateDays, tempLateInterest);
                    continue;
                }

                intAmount = (int) (borrowCollection.getInterest() * interestPercent);
                txAmount = borrowCollection.getPrincipal() + intAmount;

                //收到客户对借款还款
                int interestLower = 0;//应扣除利息
                if (borrow.isTransfer()) {
                    int interest = borrowCollection.getInterest();
                    Date startAt = DateHelper.beginOfDate((Date) borrowCollection.getStartAt().clone());
                    Date collectionAt = DateHelper.beginOfDate((Date) borrowCollection.getCollectionAt().clone());
                    Date startAtYes = DateHelper.beginOfDate((Date) borrowCollection.getStartAtYes().clone());
                    Date endAt = (Date) collectionAt.clone();

                    interestLower = Math.round(interest -
                            interest * Math.max(DateHelper.diffInDays(startAtYes, endAt, false), 0) /
                                    DateHelper.diffInDays(startAt, collectionAt, false)
                    );
                    intAmount -= interestLower;//减去转让方
                }

                //利息管理费
                if (((borrow.getType() == 0) || (borrow.getType() == 4)) && intAmount > interestLower) {
                    /**
                     * '2480 : 好人好梦',1753 : 红运当头',1699 : tasklist',3966 : 苗苗606',1413 : ljc_201',1857 : fanjunle',183 : 54435410',2327 : 栗子',2432 : 高翠西'2470 : sadfsaag',2552 : sadfsaag1',2739 : sadfsaag3',3939 : TinsonCheung',893 : kobayashi',608 : 0211',1216 : zqc9988'
                     */
                    Set<String> stockholder = new HashSet<>(Arrays.asList("2480", "1753", "1699", "3966", "1413", "1857", "183", "2327", "2432", "2470", "2552", "2739", "3939", "893", "608", "1216"));
                    if (!stockholder.contains(tender.getUserId())) {
                        txFeeIn += (int) MathHelper.myRound((intAmount - interestLower) * 0.1, 2);
                    }
                }

                //逾期收入
                if ((lateDays > 0) && (lateInterest > 0)) {
                    int tempLateInterest = Math.round(tender.getValidMoney() / borrow.getMoney() * lateInterest);
                    intAmount += tempLateInterest;
                    txFeeOut += tempLateInterest * 2;
                }

                String orderId = JixinHelper.getOrderId(JixinHelper.REPAY_PREFIX);
                repay.setAccountId(borrowAccountId);
                repay.setOrderId(orderId);
                repay.setTxAmount(StringHelper.formatDouble(txAmount + txFeeIn, 100, false));
                repay.setIntAmount(StringHelper.formatDouble(intAmount, 100, false));
                repay.setTxFeeIn(StringHelper.formatDouble(txFeeIn, 100, false));
                repay.setTxFeeOut(StringHelper.formatDouble(txFeeOut, 100, false));
                repay.setProductId(StringHelper.toString(borrow.getId()));
                repay.setAuthCode(tender.getAuthCode());
                repay.setForAccountId(tenderUserThirdAccount.getAccountId());
                repayList.add(repay);

                borrowCollection.setTRepayOrderId(orderId);
                borrowCollectionService.updateById(borrowCollection);
            }
        } while (false);
    }


    /**
     * 即信批次放款  （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay) {

        Long borrowId = voThirdBatchLendRepay.getBorrowId();
        if (ObjectUtils.isEmpty(borrowId)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "borrowId不存在!"));
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

        Borrow borrow = borrowService.findById(borrowId);
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        List<LendPay> lendPayList = new ArrayList<>();
        LendPay lendPay = null;
        UserThirdAccount tenderUserThirdAccount = null;
        int sumCount = 0;
        int validMoney = 0;
        for (Tender tender : tenderList) {
            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            validMoney = tender.getValidMoney();
            sumCount += validMoney;

            lendPay = new LendPay();
            lendPay.setAccountId(tenderUserThirdAccount.getAccountId());
            lendPay.setAuthCode(tender.getAuthCode());
            lendPay.setBidFee("0");
            lendPay.setDebtFee("0");
            lendPay.setOrderId(JixinHelper.getOrderId(JixinHelper.LEND_REPAY_PREFIX));
            lendPay.setForAccountId(borrowUserThirdAccount.getAccountId());
            lendPay.setTxAmount(StringHelper.formatDouble(validMoney, 100, false));
            lendPay.setProductId(StringHelper.toString(borrowId));
            lendPayList.add(lendPay);
        }

        BatchLendPayReq request = new BatchLendPayReq();
        request.setBatchNo(jixinHelper.getBatchNo());
        request.setAcqRes(StringHelper.toString(borrowId));//存放borrowId 标id
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/lendrepay/check");
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/lendrepay/run");
        request.setTxAmount(StringHelper.formatDouble(sumCount, 100, false));
        request.setTxCounts(StringHelper.toString(lendPayList.size()));
        request.setSubPacks(GSON.toJson(lendPayList));
        request.setChannel(ChannelContant.HTML);
        BatchLendPayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_LEND_REPAY, request, BatchLendPayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "即信批次放款失败!"));
        }
        return null;
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    public void thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayCheckResp repayCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(repayCheckResp)) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(repayCheckResp.getRetCode())) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("回调失败! msg:" + repayCheckResp.getRetMsg());
        }

        log.info("=============================即信批次放款检验参数回调===========================");
        log.info("即信批次还款检验参数成功!");

        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    public void thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
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

        if (bool) {
            ResponseEntity<VoBaseResp> resp = null;
            try {
                VoRepayReq voRepayReq = GSON.fromJson(repayRunResp.getAcqRes(), new TypeToken<VoRepayReq>() {
                }.getType());
                resp = repaymentBiz.repay(voRepayReq);
            } catch (Exception e) {
                log.error("非流转标复审异常:", e);
            }
            if (ObjectUtils.isEmpty(resp)) {
                log.info("非流转标复审成功!");
            }
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
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public void thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchLendPayCheckResp lendRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchLendPayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(lendRepayCheckResp)) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(lendRepayCheckResp.getRetCode())) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("回调失败! msg:" + lendRepayCheckResp.getRetMsg());
        }

        log.info("=============================即信批次放款检验参数回调===========================");
        log.info("即信批次放款检验参数成功!");


        try {
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public void thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchLendPayRunResp lendRepayRunResp = jixinManager.callback(request, new TypeToken<BatchLendPayRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(lendRepayRunResp)) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode())) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("回调失败! msg:" + lendRepayRunResp.getRetMsg());
            bool = false;
        }

        if (bool) {
            long borrowId = NumberHelper.toLong(lendRepayRunResp.getAcqRes());
            Borrow borrow = borrowService.findById(borrowId);

            try {
                bool = borrowBiz.transferedBorrowAgainVerify(borrow);
            } catch (Exception e) {
                log.error("非流转标复审异常:", e);
            }
            if (bool) {
                log.info("非流转标复审成功!");
            }
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
     * 批次担保账户代偿
     *
     * @param voBatchBailRepayReq
     */
    public ResponseEntity<VoBaseResp> thirdBatchBailRepay(VoBatchBailRepayReq voBatchBailRepayReq) throws Exception {
        int lateInterest = 0;//逾期利息
        Double interestPercent = voBatchBailRepayReq.getInterestPercent();
        Long repaymentId = voBatchBailRepayReq.getRepaymentId();
        interestPercent = interestPercent == 0 ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        Long borrowId = borrow.getId();//借款ID

        //逾期天数
        Date nowDateOfBegin = DateHelper.beginOfDate(new Date());
        Date repayDateOfBegin = DateHelper.beginOfDate(borrowRepayment.getRepayAt());
        int lateDays = DateHelper.diffInDays(nowDateOfBegin, repayDateOfBegin, false);
        lateDays = lateDays < 0 ? 0 : lateDays;
        if (0 < lateDays) {
            int overPrincipal = borrowRepayment.getPrincipal();
            if (borrowRepayment.getOrder() < (borrow.getTotalOrder() - 1)) {
                Specification<BorrowRepayment> brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrowId)
                        .eq("status", 0)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                Preconditions.checkNotNull(borrowRepayment, "还款不存在");

                overPrincipal = 0;
                for (BorrowRepayment temp : borrowRepaymentList) {
                    overPrincipal += temp.getPrincipal();
                }
            }
            lateInterest = (int) MathHelper.myRound(overPrincipal * 0.004 * lateDays, 2);
        }

        List<BailRepay> bailRepayList = new ArrayList<>();
        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            bailRepayList = new ArrayList<>();
            receivedBailReapy(bailRepayList, borrow, borrowUserThirdAccount.getAccountId(), borrowRepayment.getOrder(), interestPercent, lateDays, lateInterest / 2);
        }

        double txAmount = 0;
        for (BailRepay bailRepay : bailRepayList) {
            txAmount += NumberHelper.toDouble(bailRepay.getTxAmount());
        }

        BatchBailRepayReq request = new BatchBailRepayReq();
        request.setChannel(ChannelContant.HTML);
        request.setBatchNo(jixinHelper.getBatchNo());
        request.setAccountId(borrow.getBailAccountId());
        request.setProductId(StringHelper.toString(borrowId));
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setTxCounts(StringHelper.toString(bailRepayList.size()));
        request.setNotifyURL(webDomain + "/v2/third/batch/bailrepay/check");
        request.setRetNotifyURL(webDomain + "/v2/third/batch/bailrepay/run");
        request.setAcqRes(StringHelper.toString(repaymentId));
        request.setSubPacks(GSON.toJson(bailRepayList));
        BatchBailRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchBailRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次担保账户代偿失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("批次担保账户代偿成功!"));
    }

    /**
     * 收到代偿还款
     *
     * @param borrow
     * @param order
     * @param interestPercent
     * @param borrowAccountId 借款方即信存管账户id
     * @param lateDays
     * @param lateInterest
     * @return
     * @throws Exception
     */
    private void receivedBailReapy(List<BailRepay> repayList, Borrow borrow, String borrowAccountId, int order, double interestPercent, int lateDays, int lateInterest) throws Exception {
        do {
            BailRepay bailRepay = new BailRepay();
            int txFeeIn = 0;//收款手续费
            int txAmount = 0;//融资人实际付出金额=交易金额+交易利息+还款手续费
            int intAmount = 0;//交易利息

            //===================================还款校验==========================================
            if (ObjectUtils.isEmpty(borrow)) {
                break;
            }

            Long borrowId = borrow.getId();
            Specification<Tender> specification = Specifications
                    .<Tender>and()
                    .eq("status", 1)
                    .eq("borrowId", borrowId)
                    .build();

            List<Tender> tenderList = tenderService.findList(specification);
            if (CollectionUtils.isEmpty(tenderList)) {
                break;
            }

            List<Long> userIds = new ArrayList<>();
            List<Long> tenderIds = new ArrayList<>();
            for (Tender tender : tenderList) {
                userIds.add(tender.getUserId());
                tenderIds.add(tender.getId());
            }

            Specification<UserCache> ucs = Specifications
                    .<UserCache>and()
                    .in("userId", userIds.toArray())
                    .build();

            List<UserCache> userCacheList = userCacheService.findList(ucs);
            if (CollectionUtils.isEmpty(userCacheList)) {
                break;
            }

            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tenderId", tenderIds.toArray())
                    .eq("status", 0)
                    .eq("order", order)
                    .build();

            List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
            if (CollectionUtils.isEmpty(borrowCollectionList)) {
                break;
            }
            //==================================================================================
            UserThirdAccount tenderUserThirdAccount = null;
            for (Tender tender : tenderList) {
                tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
                //获取当前借款的回款记录
                BorrowCollection borrowCollection = null;
                for (int i = 0; i < borrowCollectionList.size(); i++) {
                    borrowCollection = borrowCollectionList.get(i);
                    if (StringHelper.toString(tender.getId()).equals(StringHelper.toString(borrowCollection.getTenderId()))) {
                        break;
                    }
                    borrowCollection = null;
                    continue;
                }

                if (tender.getTransferFlag() == 1) {//转让中
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .in("status", 0, 1)
                            .build();

                    List<Borrow> borrowList = borrowService.findList(bs);
                    if (CollectionUtils.isEmpty(borrowList)) {
                        continue;
                    }
                }

                if (tender.getTransferFlag() == 2) { //已转让
                    Specification<Borrow> bs = Specifications
                            .<Borrow>and()
                            .eq("tenderId", tender.getId())
                            .eq("status", 3)
                            .build();

                    List<Borrow> borrowList = borrowService.findList(bs);
                    if (CollectionUtils.isEmpty(borrowList)) {
                        continue;
                    }

                    Borrow tempBorrow = borrowList.get(0);
                    int tempOrder = order + tempBorrow.getTotalOrder() - borrow.getTotalOrder();
                    int tempLateInterest = tender.getValidMoney() / borrow.getMoney() * lateInterest;
                    int accruedInterest = 0;
                    if (tempOrder == 0) {//如果是转让后第一期回款, 则计算转让者首期应计利息
                        int interest = borrowCollection.getInterest();
                        Date startAt = DateHelper.beginOfDate((Date) borrowCollection.getStartAt().clone());//获取00点00分00秒
                        Date collectionAt = DateHelper.beginOfDate((Date) borrowCollection.getCollectionAt().clone());
                        Date startAtYes = DateHelper.beginOfDate((Date) borrowCollection.getStartAtYes().clone());
                        Date endAt = DateHelper.beginOfDate((Date) tempBorrow.getSuccessAt().clone());

                        if (endAt.getTime() > collectionAt.getTime()) {
                            endAt = (Date) collectionAt.clone();
                        }

                        accruedInterest = Math.round(interest *
                                Math.max(DateHelper.diffInDays(startAtYes, endAt, false), 0) /
                                DateHelper.diffInDays(startAt, collectionAt, false));

                        if (accruedInterest > 0) {
                            //利息管理费
                            txFeeIn += (accruedInterest * 0.1);
                        }
                    }

                    //回调
                    receivedBailReapy(repayList, tempBorrow, borrowAccountId, tempOrder, interestPercent, lateDays, tempLateInterest);

                    continue;
                }

                intAmount = (int) (borrowCollection.getInterest() * interestPercent);
                int principal = borrowCollection.getPrincipal();

                //收到客户对借款还款
                int interestLower = 0;//应扣除利息
                if (borrow.isTransfer()) {
                    int interest = borrowCollection.getInterest();
                    Date startAt = DateHelper.beginOfDate((Date) borrowCollection.getStartAt().clone());
                    Date collectionAt = DateHelper.beginOfDate((Date) borrowCollection.getCollectionAt().clone());
                    Date startAtYes = DateHelper.beginOfDate((Date) borrowCollection.getStartAtYes().clone());
                    Date endAt = (Date) collectionAt.clone();

                    interestLower = Math.round(interest -
                            interest * Math.max(DateHelper.diffInDays(startAtYes, endAt, false), 0) /
                                    DateHelper.diffInDays(startAt, collectionAt, false)
                    );
                    intAmount -= interestLower;//减去转让方
                }

                //利息管理费
                if (((borrow.getType() == 0) || (borrow.getType() == 4)) && intAmount > interestLower) {
                    /**
                     * '2480 : 好人好梦',1753 : 红运当头',1699 : tasklist',3966 : 苗苗606',1413 : ljc_201',1857 : fanjunle',183 : 54435410',2327 : 栗子',2432 : 高翠西'2470 : sadfsaag',2552 : sadfsaag1',2739 : sadfsaag3',3939 : TinsonCheung',893 : kobayashi',608 : 0211',1216 : zqc9988'
                     */
                    Set<String> stockholder = new HashSet<>(Arrays.asList("2480", "1753", "1699", "3966", "1413", "1857", "183", "2327", "2432", "2470", "2552", "2739", "3939", "893", "608", "1216"));
                    if (!stockholder.contains(tender.getUserId())) {
                        txFeeIn += (int) MathHelper.myRound((intAmount - interestLower) * 0.1, 2);
                    }
                }

                //逾期收入
                if ((lateDays > 0) && (lateInterest > 0)) {
                    int tempLateInterest = Math.round(tender.getValidMoney() / borrow.getMoney() * lateInterest);
                    intAmount += tempLateInterest;
                }

                String orderId = JixinHelper.getOrderId(JixinHelper.REPAY_BAIL_PREFIX);

                intAmount -= txFeeIn;
                txAmount = principal + intAmount;
                bailRepay.setOrderId(JixinHelper.getOrderId(JixinHelper.REPAY_BAIL_PREFIX));
                bailRepay.setTxAmount(StringHelper.formatDouble(txAmount, 100, false));
                bailRepay.setTxCapAmout(StringHelper.formatDouble(principal, 100, false));
                bailRepay.setTxIntAmount(StringHelper.formatDouble(intAmount, 100, false));
                bailRepay.setOrgOrderId(StringHelper.toString(tender.getThirdTenderOrderId()));
                bailRepay.setOrgTxAmount(StringHelper.formatDouble(tender.getValidMoney(), 100, false));
                bailRepay.setForAccountId(tenderUserThirdAccount.getAccountId());
                repayList.add(bailRepay);

                borrowCollection.setTRepayOrderId(orderId);
                borrowCollectionService.updateById(borrowCollection);
            }
        } while (false);
    }

    /**
     * 批次担保账户代偿参数检查回调
     */
    public void thirdBatchBailRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayCheckResp batchBailRepayCheckResp = jixinManager.callback(request, new TypeToken<BatchBailRepayCheckResp>() {
        });
        if (ObjectUtils.isEmpty(batchBailRepayCheckResp)) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(batchBailRepayCheckResp.getRetCode())) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("回调失败! msg:" + batchBailRepayCheckResp.getRetMsg());
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
     * 批次担保账户代偿业务处理回调
     */
    public void thirdBatchBailRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayRunResp batchBailRepayRunResp = jixinManager.callback(request, new TypeToken<BatchBailRepayRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(batchBailRepayRunResp)) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(batchBailRepayRunResp.getRetCode())) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("回调失败! msg:" + batchBailRepayRunResp.getRetMsg());
            bool = false;
        }

        if (bool) {
            long repaymentId = NumberHelper.toLong(batchBailRepayRunResp.getAcqRes());
            ResponseEntity<VoBaseResp> resp = null;
            try {
                VoAdvanceReq voAdvanceReq = new VoAdvanceReq();
                voAdvanceReq.setRepaymentId(repaymentId);
                voAdvanceReq.setBailRepayRunList(GSON.fromJson(GSON.toJson(batchBailRepayRunResp.getSubPacks()), new TypeToken<List<BailRepayRun>>() {
                }.getType()));
                resp = repaymentBiz.advanceDeal(voAdvanceReq);
            } catch (Exception e) {
                log.error("垫付异常:", e);
            }
            if (ObjectUtils.isEmpty(resp)) {
                log.info("垫付成功!");
            } else {
                log.error("垫付失败:" + resp.getBody().getState().getMsg());
            }
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
     * 批次融资人还担保账户垫款
     *
     * @param voBatchRepayBailReq
     */
    public ResponseEntity<VoBaseResp> thirdBatchRepayBail(VoBatchRepayBailReq voBatchRepayBailReq) {
        Long repaymentId = voBatchRepayBailReq.getRepaymentId();
        BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);

        Long borrowId = borrowRepayment.getBorrowId();
        Borrow borrow = borrowService.findById(borrowId);
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowId)
                .build();

        List<Tender> tenderList = tenderService.findList(specification);
        if (CollectionUtils.isEmpty(tenderList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "投标信息不存在！"));
        }

        List<Long> tenderIds = new ArrayList<>();
        for (Tender tender : tenderList) {
            tenderIds.add(tender.getId());
        }

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("order", borrowRepayment.getOrder())
                .build();

        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        if (CollectionUtils.isEmpty(borrowCollectionList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "回款信息不存在！"));
        }
        //==================================================================================
        List<RepayBail> repayBailList = new ArrayList<>();
        RepayBail repayBail = null;
        int collectionMoneyYes = 0;
        int principal = 0;
        int sumPrincipal = 0;
        for (Tender tender : tenderList) {
            repayBail = new RepayBail();
            //获取当前借款的回款记录
            BorrowCollection borrowCollection = null;
            for (int i = 0; i < borrowCollectionList.size(); i++) {
                borrowCollection = borrowCollectionList.get(i);
                if (StringHelper.toString(tender.getId()).equals(StringHelper.toString(borrowCollection.getTenderId()))) {
                    break;
                }
                borrowCollection = null;
                continue;
            }
            sumPrincipal += principal;
            repayBail.setAccountId(borrow.getBailAccountId());
            repayBail.setForAccountId(borrowUserThirdAccount.getAccountId());
            repayBail.setOrderId(JixinHelper.getOrderId(JixinHelper.BAIL_REPAY_PREFIX));
            repayBail.setTxAmount(StringHelper.formatDouble(principal, 100, false));
            repayBail.setIntAmount(StringHelper.formatDouble(collectionMoneyYes - principal, 100, false));
            repayBail.setTxFeeOut(StringHelper.formatDouble(borrowCollection.getLateInterest(), 100, false));
            repayBail.setOrgOrderId(tender.getThirdTenderOrderId());
            repayBail.setAuthCode(borrowCollection.getTBailAuthCode());
            repayBailList.add(repayBail);
        }

        Map<String, Object> acqRes = new HashMap<>();
        acqRes.put("repaymentId", repaymentId);
        acqRes.put("repayMoney", voBatchRepayBailReq.getRepayMoney());
        acqRes.put("lateInterest", voBatchRepayBailReq.getLateInterest());

        BatchRepayBailReq request = new BatchRepayBailReq();
        request.setBatchNo(jixinHelper.getBatchNo());
        request.setTxAmount(StringHelper.formatDouble(sumPrincipal, 100, false));
        request.setSubPacks(GSON.toJson(repayBailList));
        request.setTxCounts(StringHelper.toString(repayBailList.size()));
        request.setNotifyURL(webDomain + "/v2/third/batch/repaybail/check");
        request.setRetNotifyURL(webDomain + "/v2/third/batch/repaybail/run");
        request.setAcqRes(GSON.toJson(acqRes));
        request.setChannel(ChannelContant.HTML);
        BatchRepayBailResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY_BAIL, request, BatchRepayBailResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次融资人还担保账户垫款失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("批次融资人还担保账户垫款成功!"));
    }

    /**
     * 批次融资人还担保账户垫款参数检查回调
     *
     * @param request
     * @param response
     */

    public void thirdBatchRepayBailCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayBailCheckResp batchRepayBailCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayBailCheckResp>() {
        });
        if (ObjectUtils.isEmpty(batchRepayBailCheckResp)) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(batchRepayBailCheckResp.getRetCode())) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("回调失败! msg:" + batchRepayBailCheckResp.getRetMsg());
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
     * 批次融资人还担保账户垫款业务处理回调
     *
     * @param request
     * @param response
     */
    public void thirdBatchRepayBailRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayBailRunResp batchRepayBailRunResp = jixinManager.callback(request, new TypeToken<BatchRepayBailRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(batchRepayBailRunResp)) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(batchRepayBailRunResp.getRetCode())) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("回调失败! msg:" + batchRepayBailRunResp.getRetMsg());
            bool = false;
        }

        if (bool) {
            try {
                Map<String, Object> acqRes = GSON.fromJson(batchRepayBailRunResp.getAcqRes(), new TypeToken<Map<String, Object>>() {
                }.getType());
                long repaymentId = NumberHelper.toLong(StringHelper.toString(acqRes.get("repaymentId")));
                BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);
                Preconditions.checkNotNull(borrowRepayment, "还款记录不存在!");
                Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
                Preconditions.checkNotNull(borrow, "借款记录不存在!");

                int repayMoney = NumberHelper.toInt(StringHelper.toString(acqRes.get("repayMoney")));
                int lateInterest = NumberHelper.toInt(StringHelper.toString(acqRes.get("lateInterest")));

                //调用垫付逻辑
                AdvanceLog advanceLog = advanceLogService.findById(repaymentId);
                Preconditions.checkNotNull(advanceLog, "垫付记录不存在!请联系客服");

                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.IncomeOther);
                entity.setUserId(advanceLog.getUserId());
                entity.setMoney(repayMoney + lateInterest);
                entity.setRemark("收到客户对借款[" + BorrowHelper.getBorrowLink(borrowRepayment.getBorrowId(), borrow.getName()) + "]第" + (borrowRepayment.getOrder() + 1) + "期垫付的还款");
                capitalChangeHelper.capitalChange(entity);

                //
                advanceLog.setStatus(1);
                advanceLog.setRepayAtYes(new Date());
                advanceLog.setRepayMoneyYes(repayMoney + lateInterest);
                advanceLogService.updateById(advanceLog);

            } catch (Exception e) {
                log.error("borrowRepaymentThirdBizImpl 资产变更异常：", e);
            }
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
