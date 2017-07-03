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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    @Value(value = "${jixin.redPacketAccountId}")
    private String redPacketAccountId; //存管红包账户

    /**
     * 即信批次还款
     *
     * @param voThirdBatchRepay
     * @return
     */
    public ResponseEntity<VoBaseResp> thirdBatchRepay(VoThirdBatchRepay voThirdBatchRepay) throws Exception {
        Date nowDate = new Date();
        Long repaymentId = voThirdBatchRepay.getRepaymentId();
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);

        List<Repay> repayList = null;
        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            repayList = getRepayList(voThirdBatchRepay);
        } else {
            /**
             * @// TODO: 2017/7/1
             * 垫付时借款人没有收取  颠覆人管理费用
             * 还垫付需要连贯起来
             *
             * //借款人逾期罚息
             */
            //批次融资人还担保账户垫款
            VoBatchRepayBailReq voBatchRepayBailReq = new VoBatchRepayBailReq();
            voBatchRepayBailReq.setRepaymentId(repaymentId);
            return thirdBatchRepayBail(voBatchRepayBailReq);
        }

        if (CollectionUtils.isEmpty(repayList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "还款不存在"));
        }

        double txAmount = 0;
        for (Repay repay : repayList) {
            txAmount += NumberHelper.toDouble(repay.getTxAmount());
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
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repay/run");
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repay/check");
        request.setAcqRes(GSON.toJson(voThirdBatchRepay));
        request.setSubPacks(GSON.toJson(repayList));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(repayList.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            new Exception("即信批次还款失败：" + response.getRetMsg());
        }
        return ResponseEntity.ok(VoBaseResp.ok("还款成功"));
    }

    /**
     * 获取即信还款集合
     *
     * @param voThirdBatchRepay
     * @return
     * @throws Exception
     */
    public List<Repay> getRepayList(VoThirdBatchRepay voThirdBatchRepay) throws Exception {
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
            receivedRepay(repayList, borrow, borrowUserThirdAccount.getAccountId(), borrowRepayment.getOrder(), interestPercent, lateDays, lateInterest / 2);
        }
        return repayList;
    }

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
    public void receivedRepay(List<Repay> repayList, Borrow borrow, String borrowAccountId, int order, double interestPercent, int lateDays, int lateInterest) throws Exception {
        do {
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
            Repay repay = null;
            int txFeeIn = 0;//投资方手续费  利息管理费
            int txAmount = 0;//融资人实际付出金额=交易金额+交易利息+还款手续费
            int intAmount = 0;//交易利息
            int txFeeOut = 0;//借款方手续费  逾期利息
            for (Tender tender : tenderList) {
                repay = new Repay();
                txFeeIn = 0;
                txAmount = 0;
                intAmount = 0;
                txFeeOut = 0;

                tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());//投标人银行存管账户
                BorrowCollection borrowCollection = null;//当前借款的回款记录
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

                    //回调
                    receivedRepay(repayList, tempBorrow, borrowAccountId, tempOrder, interestPercent, lateDays, tempLateInterest);
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
                            interest * Math.max(DateHelper.diffInDays(endAt, startAtYes, false), 0) / DateHelper.diffInDays(collectionAt, startAt, false)
                    );

                    //债权购买人应扣除利息
                    txFeeIn += interestLower;
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

                //借款人逾期罚息
                if ((lateDays > 0) && (lateInterest > 0)) {
                    txFeeOut += lateInterest;
                }

                String orderId = JixinHelper.getOrderId(JixinHelper.REPAY_PREFIX);
                repay.setAccountId(borrowAccountId);
                repay.setOrderId(orderId);
                repay.setTxAmount(StringHelper.formatDouble(txAmount + txFeeIn, 100, false));
                repay.setIntAmount(StringHelper.formatDouble(intAmount, 100, false));
                repay.setTxFeeIn(StringHelper.formatDouble(txFeeIn, 100, false));
                repay.setTxFeeOut(StringHelper.formatDouble(txFeeOut, 100, false));
                repay.setProductId(borrow.getProductId());
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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay) throws Exception {
        Date nowDate = new Date();
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
        UserThirdAccount takeUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());//收款人存管账户记录

        Long takeUserId = borrow.getTakeUserId();
        if (!ObjectUtils.isEmpty(takeUserId)) {
            takeUserThirdAccount = userThirdAccountService.findByUserId(takeUserId);
        }

        //净值账户管理费
        double fee = 0;
        if (borrow.getType() == 1) {
            double manageFeeRate = 0.0012;
            if (borrow.getRepayFashion() == 1) {
                fee = MathHelper.myRound(borrow.getMoney() * manageFeeRate / 30 * borrow.getTimeLimit(), 2);
            } else {
                fee = MathHelper.myRound(borrow.getMoney() * manageFeeRate * borrow.getTimeLimit(), 2);
            }
        }

        List<LendPay> lendPayList = new ArrayList<>();
        LendPay lendPay = null;
        UserThirdAccount tenderUserThirdAccount = null;
        int sumCount = 0;
        int validMoney = 0;
        int debtFee = 0;
        for (Tender tender : tenderList) {
            debtFee = 0;

            tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
            validMoney = tender.getValidMoney();//投标有效金额
            sumCount += validMoney; //放款总金额

            //添加奖励
            if (borrow.getAwardType() > 0) {
                int money = (int) MathHelper.myRound((tender.getValidMoney() / borrow.getMoney()) * borrow.getAward(), 2);
                if (borrow.getAwardType() == 2) {
                    money = (int) MathHelper.myRound(tender.getValidMoney() * borrow.getAward() / 100, 2);
                }
                debtFee += money;
            }

            //净值账户管理费
            if (borrow.getType() == 1) {
                debtFee += validMoney / borrow.getMoney() * fee;
            }

            lendPay = new LendPay();
            lendPay.setAccountId(tenderUserThirdAccount.getAccountId());
            lendPay.setAuthCode(tender.getAuthCode());
            lendPay.setBidFee("0");
            lendPay.setDebtFee(StringHelper.formatDouble(debtFee, 100, false));
            lendPay.setOrderId(JixinHelper.getOrderId(JixinHelper.LEND_REPAY_PREFIX));
            lendPay.setForAccountId(takeUserThirdAccount.getAccountId());
            lendPay.setTxAmount(StringHelper.formatDouble(validMoney, 100, false));
            lendPay.setProductId(borrow.getProductId());
            lendPayList.add(lendPay);
        }

        //记录日志
        String batchNo = jixinHelper.getBatchNo();
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchNoTypeContant.BATCH_LEND_REPAY);
        thirdBatchLog.setRemark("即信批次放款");
        thirdBatchLogService.save(thirdBatchLog);

        BatchLendPayReq request = new BatchLendPayReq();
        request.setBatchNo(batchNo);
        request.setAcqRes(StringHelper.toString(borrowId));//存放borrowId 标id
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/lendrepay/check");
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/lendrepay/run");
        request.setTxAmount(StringHelper.formatDouble(sumCount, 100, false));
        request.setTxCounts(StringHelper.toString(lendPayList.size()));
        request.setSubPacks(GSON.toJson(lendPayList));
        request.setChannel(ChannelContant.HTML);
        BatchLendPayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_LEND_REPAY, request, BatchLendPayResp.class);
        String retCode = response.getRetCode();
        if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.SUCCESS.equals(retCode))) {
            throw new Exception("即信批次放款失败:" + response.getRetMsg());
        }
        if ((ObjectUtils.isEmpty(response)) || (!ObjectUtils.isEmpty(retCode) && !JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            throw new Exception("即信批次放款失败!");
        }
        return null;
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        BatchRepayCheckResp repayCheckResp = jixinManager.callback(request, new TypeToken<BatchRepayCheckResp>() {
        });

        if (ObjectUtils.isEmpty(repayCheckResp)) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("请求体为空!");
        }

        if (!JixinResultContants.SUCCESS.equals(repayCheckResp.getRetCode())) {
            log.error("=============================即信批次还款检验参数回调===========================");
            log.error("回调失败! msg:" + repayCheckResp.getRetMsg());
        } else {
            log.info("=============================即信批次放款检验参数回调===========================");
            log.info("即信批次还款检验参数成功!");
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
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
                VoRepayReq voRepayReq = GSON.fromJson(repayRunResp.getAcqRes(), new TypeToken<VoRepayReq>() {
                }.getType());
                resp = repaymentBiz.repay(voRepayReq);
            } catch (Exception e) {
                log.error("还款异常:", e);
            }
            if (ObjectUtils.isEmpty(resp)) {
                log.info("还款成功!");
            }
        } else {
            log.info("还款失败!");
        }
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

        if (!JixinResultContants.SUCCESS.equals(lendRepayCheckResp.getRetCode())) {
            log.error("=============================即信批次放款检验参数回调===========================");
            log.error("回调失败! msg:" + lendRepayCheckResp.getRetMsg());
        }

        log.info("=============================即信批次放款检验参数回调===========================");
        log.info("即信批次放款检验参数成功!");


        return ResponseEntity.ok("success");
    }

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    public ResponseEntity<String> thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
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

        int num = NumberHelper.toInt(lendRepayRunResp.getFailCounts());
        if (num > 0) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("还款失败! 一共:" + num + "笔");
            bool = false;
        }

        if (bool) {
            long borrowId = NumberHelper.toLong(lendRepayRunResp.getAcqRes());
            Borrow borrow = borrowService.findById(borrowId);

            try {
                bool = borrowBiz.notTransferedBorrowAgainVerify(borrow);
            } catch (Exception e) {
                log.error("即信批次放款异常:", e);
            }
            if (bool) {
                log.info("即信批次放款成功!");
            }
        } else {
            log.info("非流转标复审失败!");
        }

        return ResponseEntity.ok("success");
    }


    /**
     * 批次担保账户代偿
     *
     * @param voBatchBailRepayReq
     */
    public ResponseEntity<VoBaseResp> thirdBatchBailRepay(VoBatchBailRepayReq voBatchBailRepayReq) throws Exception {
        Date nowDate = new Date();

        List<BailRepay> bailRepayList = getBailRepayList(voBatchBailRepayReq);
        if (CollectionUtils.isEmpty(bailRepayList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "代偿不存在"));
        }

        Long repaymentId = voBatchBailRepayReq.getRepaymentId();
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        Long borrowId = borrow.getId();//借款ID

        double txAmount = 0;
        for (BailRepay bailRepay : bailRepayList) {
            txAmount += NumberHelper.toDouble(bailRepay.getTxAmount());
        }

        //记录日志
        String batchNo = jixinHelper.getBatchNo();
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchNoTypeContant.BATCH_REPAY);
        thirdBatchLog.setRemark("即信批次还款");
        thirdBatchLogService.save(thirdBatchLog);

        BatchBailRepayReq request = new BatchBailRepayReq();
        request.setChannel(ChannelContant.HTML);
        request.setBatchNo(batchNo);
        request.setAccountId(borrow.getBailAccountId());
        request.setProductId(borrow.getProductId());
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setTxCounts(StringHelper.toString(bailRepayList.size()));
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/bailrepay/check");
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/bailrepay/run");
        request.setAcqRes(StringHelper.toString(repaymentId));
        request.setSubPacks(GSON.toJson(bailRepayList));
        BatchBailRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_BAIL_REPAY, request, BatchBailRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次担保账户代偿失败!"));
        }
        return ResponseEntity.ok(VoBaseResp.ok("批次担保账户代偿成功!"));
    }


    /**
     * 获取担保人代偿集合
     *
     * @param voBatchBailRepayReq
     * @return
     * @throws Exception
     */
    private List<BailRepay> getBailRepayList(VoBatchBailRepayReq voBatchBailRepayReq) throws Exception {
        Double interestPercent = voBatchBailRepayReq.getInterestPercent();
        Long repaymentId = voBatchBailRepayReq.getRepaymentId();
        interestPercent = ObjectUtils.isEmpty(interestPercent) ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());

        List<BailRepay> bailRepayList = new ArrayList<>();
        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            bailRepayList = new ArrayList<>();
            receivedBailRepay(bailRepayList, borrow, borrowRepayment.getOrder(), interestPercent);
        }
        return bailRepayList;
    }

    /**
     * 担保人代偿
     *
     * @param borrow
     * @param order
     * @param interestPercent
     * @return
     * @throws Exception
     */
    private void receivedBailRepay(List<BailRepay> repayList, Borrow borrow, int order, double interestPercent) throws Exception {
        do {
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
            BailRepay bailRepay = null;
            int txFeeIn = 0;//投资方手续费  利息管理费
            int txAmount = 0;//融资人实际付出金额=交易金额+交易利息+还款手续费
            int intAmount = 0;//交易利息
            int principal = 0;
            for (Tender tender : tenderList) {
                bailRepay = new BailRepay();
                txFeeIn = 0;
                intAmount = 0;
                txAmount = 0;
                principal = 0;

                tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());//投标人银行存管账户
                BorrowCollection borrowCollection = null;//当前借款的回款记录
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
                    //回调
                    receivedBailRepay(repayList, tempBorrow, tempOrder, interestPercent);

                    continue;
                }

                intAmount = (int) (borrowCollection.getInterest() * interestPercent);
                principal = borrowCollection.getPrincipal();

                //收到客户对借款还款
                int interestLower = 0;//应扣除利息
                if (borrow.isTransfer()) {
                    int interest = borrowCollection.getInterest();
                    Date startAt = DateHelper.beginOfDate((Date) borrowCollection.getStartAt().clone());
                    Date collectionAt = DateHelper.beginOfDate((Date) borrowCollection.getCollectionAt().clone());
                    Date endAt = (Date) collectionAt.clone();
                    Date startAtYes = DateHelper.beginOfDate((Date) borrowCollection.getStartAtYes().clone());

                    interestLower = Math.round(interest -
                            interest * Math.max(DateHelper.diffInDays(endAt, startAtYes, false), 0) / DateHelper.diffInDays(collectionAt, startAt, false)
                    );

                    //债权购买人应扣除利息
                    txFeeIn += interestLower;
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

                txAmount = principal + intAmount;

                String orderId = JixinHelper.getOrderId(JixinHelper.REPAY_BAIL_PREFIX);
                bailRepay.setOrderId(orderId);
                bailRepay.setTxAmount(StringHelper.formatDouble(txAmount, 100, false));
                bailRepay.setTxCapAmout(StringHelper.formatDouble(principal, 100, false));
                bailRepay.setTxIntAmount(StringHelper.formatDouble(intAmount - txFeeIn, 100, false));
                bailRepay.setOrgOrderId(StringHelper.toString(tender.getThirdTenderOrderId()));
                bailRepay.setOrgTxAmount(StringHelper.formatDouble(tender.getValidMoney(), 100, false));
                bailRepay.setForAccountId(tenderUserThirdAccount.getAccountId());
                repayList.add(bailRepay);

                borrowCollection.setTBailRepayOrderId(orderId);
                borrowCollectionService.updateById(borrowCollection);
            }
        } while (false);
    }

    /**
     * 批次担保账户代偿参数检查回调
     */
    public ResponseEntity<String> thirdBatchBailRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
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

        return ResponseEntity.ok("success");
    }

    /**
     * 批次担保账户代偿业务处理回调
     */
    public ResponseEntity<String> thirdBatchBailRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        BatchBailRepayRunResp batchBailRepayRunResp = jixinManager.callback(request, new TypeToken<BatchBailRepayRunResp>() {
        });
        boolean bool = true;
        if (ObjectUtils.isEmpty(batchBailRepayRunResp)) {
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("请求体为空!");
            bool = false;
        }

        if (!JixinResultContants.SUCCESS.equals(batchBailRepayRunResp.getRetCode())) {
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("回调失败! msg:" + batchBailRepayRunResp.getRetMsg());
            bool = false;
        }

        int num = NumberHelper.toInt(batchBailRepayRunResp.getFailCounts());
        if (num > 0) {
            log.error("=============================批次担保账户代偿业务处理回调===========================");
            log.error("批次担保账户代偿失败! 一共:" + num + "笔");
            bool = false;
        }

        if (bool) {
            long repaymentId = NumberHelper.toLong(batchBailRepayRunResp.getAcqRes());
            ResponseEntity<VoBaseResp> resp = null;
            try {
                VoAdvanceCall voAdvanceCall = new VoAdvanceCall();
                voAdvanceCall.setRepaymentId(repaymentId);
                voAdvanceCall.setBailRepayRunList(GSON.fromJson(GSON.toJson(batchBailRepayRunResp.getSubPacks()), new TypeToken<List<BailRepayRun>>() {
                }.getType()));
                resp = repaymentBiz.advanceDeal(voAdvanceCall);
            } catch (Exception e) {
                log.error("垫付异常:", e);
            }
            if (ObjectUtils.isEmpty(resp)) {
                log.info("垫付成功!");
            } else {
                log.error("垫付失败:" + resp.getBody().getState().getMsg());
            }
        }

        return ResponseEntity.ok("success");
    }

    /**
     * 批次融资人还担保账户垫款
     *
     * @param voBatchRepayBailReq
     */
    public ResponseEntity<VoBaseResp> thirdBatchRepayBail(VoBatchRepayBailReq voBatchRepayBailReq) throws Exception {
        Date nowDate = new Date();
        int lateInterest = 0;//逾期利息
        Double interestPercent = voBatchRepayBailReq.getInterestPercent();
        Long repaymentId = voBatchRepayBailReq.getRepaymentId();
        interestPercent = ObjectUtils.isEmpty(interestPercent) ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        Long borrowId = borrow.getId();//借款ID

        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        //逾期天数
        Date nowDateOfBegin = DateHelper.beginOfDate(new Date());
        Date repayDateOfBegin = DateHelper.beginOfDate(borrowRepayment.getRepayAt());
        int lateDays = DateHelper.diffInDays(nowDateOfBegin, repayDateOfBegin, false);
        lateDays = lateDays < 0 ? 0 : lateDays;
        if (0 < lateDays) {
            int overPrincipal = borrowRepayment.getPrincipal();
            if (borrowRepayment.getOrder() < (borrow.getTotalOrder() - 1)) {
                Specification<BorrowRepayment> brs = Specifications.<BorrowRepayment>and()
                        .eq("status", 0)
                        .eq("borrowId", borrowId)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                Preconditions.checkNotNull(borrowRepayment, "还款信息不存在");

                overPrincipal = 0;
                for (BorrowRepayment temp : borrowRepaymentList) {
                    overPrincipal += temp.getPrincipal();
                }
            }
            lateInterest = (int) MathHelper.myRound(overPrincipal * 0.004 * lateDays, 2);
        }

        List<RepayBail> repayBails = null;
        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            repayBails = new ArrayList<>();
            receivedRepayBail(repayBails, borrow, borrowUserThirdAccount.getAccountId(), borrowRepayment.getOrder(), interestPercent, lateInterest);
        }

        if (CollectionUtils.isEmpty(repayBails)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "代偿不存在"));
        }

        double txAmount = 0;
        for (RepayBail bailRepay : repayBails) {
            txAmount += NumberHelper.toDouble(bailRepay.getTxAmount());
        }

        Map<String, Object> acqRes = new HashMap<>();
        acqRes.put("repaymentId", repaymentId);
        acqRes.put("repayMoney", borrowRepayment.getRepayMoney());
        acqRes.put("lateInterest", lateInterest);

        //记录日志
        String batchNo = jixinHelper.getBatchNo();
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchNoTypeContant.BATCH_REPAY);
        thirdBatchLog.setRemark("即信批次还款");
        thirdBatchLogService.save(thirdBatchLog);

        BatchRepayBailReq request = new BatchRepayBailReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(txAmount, 100, false));
        request.setSubPacks(GSON.toJson(repayBails));
        request.setTxCounts(StringHelper.toString(repayBails.size()));
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repaybail/check");
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repaybail/run");
        request.setAcqRes(GSON.toJson(acqRes));
        request.setChannel(ChannelContant.HTML);
        BatchRepayBailResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY_BAIL, request, BatchRepayBailResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次融资人还担保账户垫款失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("批次融资人还担保账户垫款成功!"));
    }

    /**
     * 收到代偿还款
     *
     * @param borrow
     * @param order
     * @param interestPercent
     * @param lateInterest
     * @return
     * @throws Exception
     */
    private void receivedRepayBail(List<RepayBail> repayBails, Borrow borrow, String borrowUserThirdAccount, int order, double interestPercent, int lateInterest) throws Exception {
        do {
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
            RepayBail repayBail = null;
            int txAmount = 0;//融资人实际付出金额=交易金额+交易利息+还款手续费
            int intAmount = 0;//交易利息
            int principal = 0;
            int txFeeOut = 0;
            for (Tender tender : tenderList) {
                repayBail = new RepayBail();
                txAmount = 0;
                intAmount = 0;
                txFeeOut = 0;

                BorrowCollection borrowCollection = null;//当前借款的回款记录
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

                    //回调
                    receivedRepayBail(repayBails, tempBorrow, borrowUserThirdAccount, tempOrder, interestPercent, tempLateInterest);
                    continue;
                }

                intAmount = (int) (borrowCollection.getInterest() * interestPercent);
                principal = borrowCollection.getPrincipal();


                //借款人逾期罚息
                if (lateInterest > 0) {
                    txFeeOut += lateInterest;
                }

                txAmount = principal + intAmount;

                String orderId = JixinHelper.getOrderId(JixinHelper.BAIL_REPAY_PREFIX);
                repayBail.setOrderId(orderId);
                repayBail.setAccountId(borrowUserThirdAccount);
                repayBail.setTxAmount(StringHelper.formatDouble(txAmount, 100, false));
                repayBail.setIntAmount(StringHelper.formatDouble(intAmount, 100, false));
                repayBail.setForAccountId(borrow.getBailAccountId());
                repayBail.setTxFeeOut(StringHelper.formatDouble(txFeeOut, 100, false));
                repayBail.setOrgOrderId(borrowCollection.getTBailRepayOrderId());
                repayBail.setAuthCode(borrowCollection.getTBailAuthCode());

                repayBails.add(repayBail);

                borrowCollection.setTBailRepayOrderId(orderId);
                borrowCollectionService.updateById(borrowCollection);
            }
        } while (false);
    }

    /**
     * 批次融资人还担保账户垫款参数检查回调
     *
     * @param request
     * @param response
     */

    public ResponseEntity<String> thirdBatchRepayBailCheckCall(HttpServletRequest request, HttpServletResponse response) {
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

        return ResponseEntity.ok("success");
    }

    /**
     * 批次融资人还担保账户垫款业务处理回调
     *
     * @param request
     * @param response
     */
    public ResponseEntity<String> thirdBatchRepayBailRunCall(HttpServletRequest request, HttpServletResponse response) {
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

        int num = NumberHelper.toInt(batchRepayBailRunResp.getFailCounts());
        if (num > 0) {
            log.error("=============================即信批次放款处理结果回调===========================");
            log.error("批次放款失败! 一共:" + num + "笔");
            bool = false;
        }

        if (bool) {
            try {
                Map<String, Object> acqRes = GSON.fromJson(batchRepayBailRunResp.getAcqRes(), new TypeToken<Map<String, Object>>() {
                }.getType());
                long repaymentId = NumberHelper.toLong(StringHelper.toString(acqRes.get("repaymentId")));
                BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);
                if (borrowRepayment.getStatus() != 0) {
                    log.info("立即还款：该笔借款已归还");
                    return ResponseEntity.ok("success");
                }

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

        return ResponseEntity.ok("success");

    }
}
