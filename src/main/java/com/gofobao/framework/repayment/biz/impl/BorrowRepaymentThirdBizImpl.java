package com.gofobao.framework.repayment.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_lend_pay.*;
import com.gofobao.framework.api.model.batch_repay.*;
import com.gofobao.framework.asset.entity.AdvanceLog;
import com.gofobao.framework.asset.service.AdvanceLogService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.integral.IntegralChangeEntity;
import com.gofobao.framework.common.integral.IntegralChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchRepay;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.entity.Statistic;
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
import org.springframework.messaging.MessagingException;
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
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private UserCacheService userCacheService;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    /**
     * 即信批次还款
     *
     * @param voThirdBatchRepay
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdBatchRepay(VoThirdBatchRepay voThirdBatchRepay) throws Exception{
        Date nowDate = new Date();
        int lateInterest = 0;//逾期利息
        Double interestPercent = voThirdBatchRepay.getInterestPercent();
        Long repaymentId = voThirdBatchRepay.getRepaymentId();
        Boolean isUserOpen = voThirdBatchRepay.getIsUserOpen();
        interestPercent = interestPercent == 0 ? 1 : interestPercent;
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        Long borrowId = borrow.getId();//借款ID
        int borrowType = borrow.getType();//借款type
        Long borrowUserId = borrow.getUserId();
        int repayInterest = (int) (borrowRepayment.getInterest() * interestPercent);//还款利息
        int repayMoney = borrowRepayment.getPrincipal() + repayInterest;//还款金额

        //逾期天数
        Date nowDateOfBegin = DateHelper.beginOfDate(new Date());
        Date repayDateOfBegin = DateHelper.beginOfDate(borrowRepayment.getRepayAt());
        int lateDays = DateHelper.diffInDays(nowDateOfBegin, repayDateOfBegin, false);
        lateDays = lateDays < 0 ? 0 : lateDays;
        if (0 < lateDays) {
            int overPrincipal = borrowRepayment.getPrincipal();//剩余未还本金
            if (borrowRepayment.getOrder() < (borrow.getTotalOrder() - 1)) {//计算非一次性还本付息 剩余本金

                Specification<BorrowRepayment> brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrowId)
                        .eq("status", 0)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                Preconditions.checkNotNull(borrowRepayment, "还款不存在!");

                overPrincipal = 0;
                for (BorrowRepayment temp : borrowRepaymentList) {
                    overPrincipal += temp.getPrincipal();
                }
            }

            lateInterest = (int) MathHelper.myRound(overPrincipal * 0.004 * lateDays, 2);
        }






        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            receivedReapy(borrow, borrowRepayment.getOrder(), interestPercent, lateDays, lateInterest / 2, false);
        } else {
            /**
             * @// TODO: 2017/6/9 垫付
             */

        }


        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        List<Repay> lendPayList = new ArrayList<>();
        Repay repay = null;
        UserThirdAccount tenderUserThirdAccount = null;
        int sumCount = 0;
        int validMoney = 0;
        List<Tender> tenderList = null;
        for (Tender tender : tenderList) {
            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            validMoney = tender.getValidMoney();
            sumCount += validMoney;

            repay = new Repay();
            repay.setAccountId(tenderUserThirdAccount.getAccountId());
            repay.setAuthCode(tender.getAuthCode());
            repay.setTxFeeIn("0");
            repay.setTxFeeOut("0");
            repay.setOrderId(JixinHelper.getOrderId(JixinHelper.LEND_PAY_PREFIX));
            repay.setForAccountId(borrowUserThirdAccount.getAccountId());
            repay.setTxAmount(StringHelper.formatDouble(validMoney, 100, false));
            repay.setProductId(StringHelper.toString(borrowId));
            lendPayList.add(repay);
        }

        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(jixinHelper.getBatchNo());
        request.setTxAmount(StringHelper.toString(sumCount));
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repay/run");
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repay/check");
        request.setAcqRes(StringHelper.toString(borrowId));
        request.setSubPacks(GSON.toJson(lendPayList));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(lendPayList.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "即信批次还款失败!"));
        }
        return null;
    }

    /**
     * 收到还款
     *
     * @param borrow
     * @param order
     * @param interestPercent
     * @param lateDays
     * @param lateInterest
     * @param advance
     * @return
     * @throws Exception
     */
    private int receivedReapy(Borrow borrow, int order, double interestPercent, int lateDays, int lateInterest, boolean advance) throws Exception {
        int rs = 1;
        do {
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
                    .in("userId", userIds)
                    .build();

            List<UserCache> userCacheList = userCacheService.findList(ucs);
            if (CollectionUtils.isEmpty(userCacheList)) {
                break;
            }

            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .in("tenderId", tenderIds)
                    .eq("status", 0)
                    .eq("`order`", order)
                    .build();

            List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
            if (CollectionUtils.isEmpty(borrowCollectionList)) {
                break;
            }

            for (Tender tender : tenderList) {

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

                    VoCancelBorrow voCancelBorrow = new VoCancelBorrow();
                    voCancelBorrow.setBorrowId(borrowList.get(0).getId());

                    //取消当前借款
                    borrowBiz.cancelBorrow(voCancelBorrow);
                    tender.setTransferFlag(0);//设置转让标识
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

                        }
                    }

                    borrowCollection.setCollectionAtYes(new Date());
                    borrowCollection.setStatus(1);
                    borrowCollection.setCollectionMoneyYes(accruedInterest);
                    borrowCollectionService.updateById(borrowCollection);

                    //回调
                    receivedReapy(tempBorrow, tempOrder, interestPercent, lateDays, tempLateInterest, advance);

                    if (tempOrder == (tempBorrow.getTotalOrder() - 1)) {
                        tempBorrow.setCloseAt(borrowCollection.getCollectionAtYes());
                        borrowService.updateById(tempBorrow);
                    }
                    continue;
                }

                int collectionInterest = (int) (borrowCollection.getInterest() * interestPercent);
                int collectionMoney = borrowCollection.getPrincipal() + collectionInterest;

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

                    Long transferUserId = borrow.getUserId();//扣除借款标转让方当期应计的利息。


                }

                //扣除待收


                //利息管理费
                if (((borrow.getType() == 0) || (borrow.getType() == 4)) && collectionInterest > interestLower) {
                    /**
                     * '2480 : 好人好梦',1753 : 红运当头',1699 : tasklist',3966 : 苗苗606',1413 : ljc_201',1857 : fanjunle',183 : 54435410',2327 : 栗子',2432 : 高翠西'2470 : sadfsaag',2552 : sadfsaag1',2739 : sadfsaag3',3939 : TinsonCheung',893 : kobayashi',608 : 0211',1216 : zqc9988'
                     */
                    Set<String> stockholder = new HashSet<>(Arrays.asList("2480", "1753", "1699", "3966", "1413", "1857", "183", "2327", "2432", "2470", "2552", "2739", "3939", "893", "608", "1216"));
                    if (!stockholder.contains(tender.getUserId())) {

                    }
                }

                //逾期收入
                if ((lateDays > 0) && (lateInterest > 0)) {
                    int tempLateInterest = Math.round(tender.getValidMoney() / borrow.getMoney() * lateInterest);

                }


            }
            rs = 0;
        } while (false);
        return rs;
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
            lendPay.setOrderId(JixinHelper.getOrderId(JixinHelper.LEND_PAY_PREFIX));
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
            log.error("回调失败!");
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

        if (ObjectUtils.isEmpty(repayRunResp)) {
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(repayRunResp.getRetCode())) {
            log.error("=============================即信批次还款处理结果回调===========================");
            log.error("回调失败! msg:" + repayRunResp.getRetMsg());
        }

        //使用批次号去进行查询

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

        if (ObjectUtils.isEmpty(lendRepayRunResp)) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(lendRepayRunResp.getRetCode())) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("回调失败! msg:" + lendRepayRunResp.getRetMsg());
        }

        long borrowId = NumberHelper.toLong(lendRepayRunResp.getAcqRes());
        Borrow borrow = borrowService.findById(borrowId);
        boolean bool = false;
        try {
            bool = borrowBiz.transferedBorrowAgainVerify(borrow);
        } catch (Exception e) {
            log.error("非流转标复审异常:", e);
        }
        if (bool) {
            log.info("非流转标复审成功!");
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
