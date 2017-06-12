package com.gofobao.framework.repayment.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.AdvanceLog;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AdvanceLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderList;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListResWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LtSpecification;
import com.gofobao.framework.common.integral.IntegralChangeEntity;
import com.gofobao.framework.common.integral.IntegralChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.helper.project.IntegralChangeHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.entity.Notices;
import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * Created by admin on 2017/6/6.
 */
@Service
@Slf4j
public class RepaymentBizImpl implements RepaymentBiz {
    final Gson GSON = new GsonBuilder().create();

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private StatisticBiz statisticBiz;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private IntegralChangeHelper integralChangeHelper;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private AdvanceLogService advanceLogService;

    /**
     * 还款计划
     *
     * @param voCollectionOrderReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewCollectionOrderListResWarpRes> repaymentList(VoCollectionOrderReq voCollectionOrderReq) {
        try {
            VoViewCollectionOrderList voViewCollectionOrderListRes = borrowRepaymentService.repaymentList(voCollectionOrderReq);
            VoViewCollectionOrderListResWarpRes voViewCollectionOrderListResWarpRes = VoBaseResp.ok("查询成功", VoViewCollectionOrderListResWarpRes.class);
            voViewCollectionOrderListResWarpRes.setListRes(voViewCollectionOrderListRes);
            return ResponseEntity.ok(voViewCollectionOrderListResWarpRes);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionOrderListResWarpRes.class));
        }
    }

    /**
     * 还款详情
     *
     * @param voInfoReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewOrderDetailWarpRes> info(VoInfoReq voInfoReq) {
        try {
            VoViewOrderDetailRes voViewOrderDetailRes = borrowRepaymentService.info(voInfoReq);
            VoViewOrderDetailWarpRes voViewOrderDetailWarpRes = VoBaseResp.ok("查询成功", VoViewOrderDetailWarpRes.class);
            voViewOrderDetailWarpRes.setDetailWarpRes(voViewOrderDetailRes);
            return ResponseEntity.ok(voViewOrderDetailWarpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewOrderDetailWarpRes.class));
        }
    }

    /**
     * 校验还款
     *
     * @param voRepayReq
     * @return
     */
    private ResponseEntity<VoBaseResp> checkRepay(VoRepayReq voRepayReq) {
        int lateInterest = 0;//逾期利息
        Double interestPercent = voRepayReq.getInterestPercent();
        Long userId = voRepayReq.getUserId();
        Long repaymentId = voRepayReq.getRepaymentId();

        interestPercent = interestPercent == 0 ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Preconditions.checkNotNull(borrowRepayment, "还款不存在!");
        if (borrowRepayment.getStatus() != 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("还款状态已发生改变!")));
        }

        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        int borrowType = borrow.getType();//借款type
        Long borrowUserId = borrow.getUserId();

        Asset borrowUserAsset = assetService.findByUserId(borrowUserId);
        Preconditions.checkNotNull(borrowRepayment, "用户资产查询失败!");


        if ((!ObjectUtils.isEmpty(userId)) && (!borrowUserId.equals(userId))) {//存在userId时 判断是否是当前用户
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("操作用户不是借款用户!")));
        }

        int repayInterest = (int) (borrowRepayment.getInterest() * interestPercent);//还款利息
        int repayMoney = borrowRepayment.getPrincipal() + repayInterest;//还款金额

        if (borrowType == 2) {
            if (borrowUserAsset.getNoUseMoney() < (borrowRepayment.getRepayMoney() + lateInterest)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("账户余额不足，请先充值!")));
            }
        } else {
            if (borrowUserAsset.getUseMoney() < MathHelper.myRound(repayMoney + lateInterest, 2)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("账户余额不足，请先充值!")));
            }
        }

        List<BorrowRepayment> borrowRepaymentList = null;
        if (borrowRepayment.getOrder() > 0) {
            Specification<BorrowRepayment> brs = Specifications
                    .<BorrowRepayment>and()
                    .eq("status", 0)
                    .predicate(new LtSpecification<BorrowRepayment>("order", new DataObject(borrowRepayment.getOrder())))
                    .build();
            borrowRepaymentList = borrowRepaymentService.findList(brs);

            if (!CollectionUtils.isEmpty(borrowRepaymentList)) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("该借款上一期还未还!")));
            }
        }
        return null;
    }

    /**
     * 立即还款
     *
     * @param voRepayReq
     * @return
     */
    public ResponseEntity<VoBaseResp> repay(VoRepayReq voRepayReq) throws Exception {
        Date nowDate = new Date();
        int lateInterest = 0;//逾期利息
        Double interestPercent = voRepayReq.getInterestPercent();
        Long repaymentId = voRepayReq.getRepaymentId();
        Boolean isUserOpen = voRepayReq.getIsUserOpen();
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

        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.Repayment);
        entity.setUserId(borrowUserId);
        entity.setMoney(repayMoney);
        entity.setInterest(repayInterest);
        entity.setRemark("对借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]第" + (borrowRepayment.getOrder() + 1) + "期的还款");
        if (borrowType == 2) {
            entity.setAsset("sub@no_use_money");
        } else if (!isUserOpen) {
            entity.setRemark("（系统自动还款）");
        } else if (interestPercent < 1) {
            entity.setRemark("（提前结清）");
        }
        try {
            capitalChangeHelper.capitalChange(entity);
        } catch (Exception e) {
            log.error("立即还款异常:", e);
        }


        //扣除待还
        entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.PaymentLower);
        entity.setUserId(borrowUserId);
        entity.setMoney(borrowRepayment.getRepayMoney());
        entity.setInterest(borrowRepayment.getInterest());
        entity.setRemark("还款成功扣除待还");
        try {
            capitalChangeHelper.capitalChange(entity);
        } catch (Exception e) {
            log.error("立即还款异常:", e);
        }


        if ((lateDays > 0) && (lateInterest > 0)) {
            entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Overdue);
            entity.setUserId(borrowUserId);
            entity.setMoney(lateInterest);
            entity.setRemark("借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的逾期罚息");
            capitalChangeHelper.capitalChange(entity);

        }

        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            receivedReapy(borrow, borrowRepayment.getOrder(), interestPercent, lateDays, lateInterest / 2, false);
        } else {
            AdvanceLog advanceLog = advanceLogService.findById(repaymentId);
            Preconditions.checkNotNull(advanceLog, "垫付记录不存在!请联系客服");

            entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.IncomeOther);
            entity.setUserId(advanceLog.getUserId());
            entity.setMoney(repayMoney + lateInterest);
            entity.setRemark("收到客户对借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]第" + (borrowRepayment.getOrder() + 1) + "期垫付的还款");
            capitalChangeHelper.capitalChange(entity);
            //
            advanceLog.setStatus(1);
            advanceLog.setRepayAtYes(new Date());
            advanceLog.setRepayMoneyYes(repayMoney + lateInterest);
            advanceLogService.updateById(advanceLog);

        }

        borrowRepayment.setStatus(1);
        borrowRepayment.setLateDays(NumberHelper.toInt(StringHelper.toString(lateDays)));
        borrowRepayment.setLateInterest(lateInterest);
        borrowRepayment.setRepayAtYes(nowDate);
        borrowRepayment.setRepayMoneyYes(repayMoney);
        borrowRepaymentService.updateById(borrowRepayment);

        if (borrowRepayment.getOrder() == (borrow.getTotalOrder() - 1)) {
            borrow.setCloseAt(borrowRepayment.getRepayAtYes());
        }
        borrow.setUpdatedAt(nowDate);
        borrowService.updateById(borrow);

        //更新统计数据
        try {
            Statistic statistic = new Statistic();

            statistic.setWaitRepayTotal((long) -repayMoney);
            if (!borrow.isTransfer()) {//判断非转让标
                if (borrow.getType() == 0) {
                    statistic.setTjWaitRepayPrincipalTotal((long) -borrowRepayment.getPrincipal());
                    statistic.setTjWaitRepayTotal((long) -repayMoney);
                } else if (borrow.getType() == 1) {
                    statistic.setJzWaitRepayPrincipalTotal((long) -borrowRepayment.getPrincipal());
                    statistic.setJzWaitRepayTotal((long) -repayMoney);
                } else if (borrow.getType() == 2) {

                } else if (borrow.getType() == 4) {
                    statistic.setQdWaitRepayPrincipalTotal((long) -borrowRepayment.getPrincipal());
                    statistic.setQdWaitRepayTotal((long) -repayMoney);
                }
            }
            if (!ObjectUtils.isEmpty(statistic)) {
                statisticBiz.caculate(statistic);
            }
        } catch (MessagingException e) {
            log.error(String.format("立即还款统计错误：", e));
        }
        return ResponseEntity.ok(VoBaseResp.ok("立即还款成功!"));
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

            //会员用户集合
            Set<Long> collectionUserIds = new HashSet<>();
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
                            CapitalChangeEntity entity = new CapitalChangeEntity();
                            entity.setType(CapitalChangeEnum.IncomeOther);
                            entity.setUserId(tender.getUserId());
                            entity.setMoney(accruedInterest);
                            entity.setRemark("收到借款标[" + BorrowHelper.getBorrowLink(tempBorrow.getId(), tempBorrow.getName()) + "]转让当期应计利息。");
                            capitalChangeHelper.capitalChange(entity);

                            //利息管理费
                            entity = new CapitalChangeEntity();
                            entity.setType(CapitalChangeEnum.InterestManager);
                            entity.setUserId(tender.getUserId());
                            entity.setMoney((int) (accruedInterest * 0.1));
                            capitalChangeHelper.capitalChange(entity);

                            Integer integral = accruedInterest * 10;
                            if (borrow.getType() == 0 && 0 < integral) {
                                IntegralChangeEntity integralChangeEntity = new IntegralChangeEntity();
                                integralChangeEntity.setUserId(borrow.getUserId());
                                integralChangeEntity.setType(IntegralChangeEnum.TENDER);
                                integralChangeEntity.setValue(integral);
                                integralChangeHelper.integralChange(integralChangeEntity);
                            }
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

                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.IncomeRepayment);
                entity.setUserId(tender.getUserId());
                entity.setToUserId(borrow.getUserId());
                entity.setMoney(collectionMoney);
                entity.setInterest(collectionInterest);
                entity.setRemark("收到客户对借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]第" + (borrowCollection.getOrder() + 1) + "期的还款");

                if (advance) {
                    entity.setRemark("收到广富宝对借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]第" + (borrowCollection.getOrder() + 1) + "期的垫付还款");
                }

                if (interestPercent < 1) {
                    entity.setRemark("（提前结清）");
                }
                capitalChangeHelper.capitalChange(entity);

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

                    Long transferUserId = borrow.getUserId();

                    entity = new CapitalChangeEntity();
                    entity.setType(CapitalChangeEnum.ExpenditureOther);
                    entity.setUserId(tender.getUserId());
                    entity.setToUserId(transferUserId);
                    entity.setMoney(interestLower);
                    entity.setRemark("扣除借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]转让方当期应计的利息。");
                    capitalChangeHelper.capitalChange(entity);
                }

                //扣除待收
                entity = new CapitalChangeEntity();
                entity.setType(CapitalChangeEnum.CollectionLower);
                entity.setUserId(tender.getUserId());
                entity.setToUserId(borrow.getUserId());
                entity.setMoney(borrowCollection.getCollectionMoney());
                entity.setInterest(borrowCollection.getInterest());
                entity.setRemark("收到客户对[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]借款的还款,扣除待收");
                capitalChangeHelper.capitalChange(entity);

                //利息管理费
                if (((borrow.getType() == 0) || (borrow.getType() == 4)) && collectionInterest > interestLower) {
                    /**
                     * '2480 : 好人好梦',1753 : 红运当头',1699 : tasklist',3966 : 苗苗606',1413 : ljc_201',1857 : fanjunle',183 : 54435410',2327 : 栗子',2432 : 高翠西'2470 : sadfsaag',2552 : sadfsaag1',2739 : sadfsaag3',3939 : TinsonCheung',893 : kobayashi',608 : 0211',1216 : zqc9988'
                     */
                    Set<String> stockholder = new HashSet<>(Arrays.asList("2480", "1753", "1699", "3966", "1413", "1857", "183", "2327", "2432", "2470", "2552", "2739", "3939", "893", "608", "1216"));
                    if (!stockholder.contains(tender.getUserId())) {
                        entity = new CapitalChangeEntity();
                        entity.setType(CapitalChangeEnum.InterestManager);
                        entity.setUserId(tender.getUserId());
                        entity.setMoney((int) MathHelper.myRound((collectionInterest - interestLower) * 0.1, 2));
                        entity.setRemark("收到借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的逾期罚息");
                        capitalChangeHelper.capitalChange(entity);
                    }
                }

                //逾期收入
                if ((lateDays > 0) && (lateInterest > 0)) {
                    int tempLateInterest = Math.round(tender.getValidMoney() / borrow.getMoney() * lateInterest);
                    entity = new CapitalChangeEntity();
                    entity.setType(CapitalChangeEnum.IncomeOverdue);
                    entity.setUserId(tender.getUserId());
                    entity.setToUserId(borrow.getUserId());
                    entity.setMoney(tempLateInterest);
                    entity.setRemark("收到借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的逾期罚息");
                    capitalChangeHelper.capitalChange(entity);
                }

                Long tenderUserId = tender.getUserId();
                if (!collectionUserIds.contains(tenderUserId)) {
                    collectionUserIds.add(tenderUserId);

                    String noticeContent = "客户在 " + DateHelper.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss") + " 已将借款["
                            + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]第" + (borrowCollection.getOrder() + 1) + "期还款,还款金额为" + StringHelper.formatDouble(collectionMoney, 100, true) + "元";
                    if (advance) {
                        noticeContent = "广富宝在" + DateHelper.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss") + " 已将借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) +
                                "]第" + (borrowCollection.getOrder() + 1) + "期垫付还款,垫付金额为" + StringHelper.formatDouble(collectionMoney, 100, true) + "元";
                    }

                    Notices notices = new Notices();
                    notices.setFromUserId(1L);
                    notices.setUserId(tenderUserId);
                    notices.setRead(false);
                    notices.setName("客户还款");
                    notices.setContent(noticeContent);
                    notices.setType("system");
                    notices.setCreatedAt(new Date());
                    notices.setUpdatedAt(new Date());
                    //发送站内信
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
                    mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
                    Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
                    mqConfig.setMsg(body);
                    try {
                        log.info(String.format("borrowProvider doAgainVerify send mq %s", GSON.toJson(body)));
                        mqHelper.convertAndSend(mqConfig);
                    } catch (Exception e) {
                        log.error("borrowProvider doAgainVerify send mq exception", e);
                    }
                }

                //投资积分
                int integral = (collectionInterest - interestLower) * 10;
                if ((borrow.getType() == 0 || borrow.getType() == 4) && 0 < integral) {
                    IntegralChangeEntity integralChangeEntity = new IntegralChangeEntity();
                    integralChangeEntity.setType(IntegralChangeEnum.TENDER);
                    integralChangeEntity.setUserId(tender.getUserId());
                    integralChangeEntity.setValue(integral);
                    integralChangeHelper.integralChange(integralChangeEntity);
                }

                borrowCollection.setCollectionAtYes(new Date());
                borrowCollection.setStatus(1);
                borrowCollection.setLateDays(NumberHelper.toInt(StringHelper.toString(lateDays)));
                borrowCollection.setLateInterest(lateInterest);
                borrowCollection.setCollectionMoneyYes(collectionMoney);

                //
                borrowCollectionService.updateById(borrowCollection);

                //更新投标
                tender.setState(3);
                tenderService.updateById(tender);

                //收到车贷标回款扣除 自身车贷标待收本金 和 推荐人的邀请用户车贷标总待收本金
                //updateUserCacheByReceivedRepay(borrowCollection, tender, borrow);
                //项目回款短信通知
                //smsNoticeByReceivedRepay(borrowCollection, tender, borrow);
                //事件event(new ReceivedRepay($collection, $tender, $borrow));
            }
            rs = 0;
        } while (false);
        return rs;
    }


    /**
     * 立即还款
     *
     * @param voInstantlyRepayment
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> instantly(VoInstantlyRepaymentReq voInstantlyRepayment) throws Exception {
        VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setUserId(voInstantlyRepayment.getUserId());
        voRepayReq.setRepaymentId(voInstantlyRepayment.getRepaymentId());
        voRepayReq.setInterestPercent(0d);
        voRepayReq.setIsUserOpen(true);
        //校验还款
        ResponseEntity<VoBaseResp> resp = checkRepay(voRepayReq);
        if (!ObjectUtils.isEmpty(resp)) {
            return resp;
        }

        //调用即信还款
        VoThirdBatchRepay voThirdBatchRepay = new VoThirdBatchRepay();
        voThirdBatchRepay.setUserId(voInstantlyRepayment.getUserId());
        voThirdBatchRepay.setRepaymentId(voInstantlyRepayment.getRepaymentId());
        voThirdBatchRepay.setInterestPercent(0d);
        voThirdBatchRepay.setIsUserOpen(true);

        return borrowRepaymentThirdBiz.thirdBatchRepay(voThirdBatchRepay);
    }

    /**
     * 垫付
     *
     * @param voAdvanceReq
     * @return
     */
    public ResponseEntity<VoBaseResp> advance(VoAdvanceReq voAdvanceReq) throws Exception {
        Long repaymentId = voAdvanceReq.getRepaymentId();
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Preconditions.checkNotNull(borrowRepayment, "还款记录不存在！");
        if (borrowRepayment.getStatus() != 0) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "还款状态已发生改变!"));
        }

        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        Preconditions.checkNotNull(borrow, "借款记录不存在！");
        if (borrow.getType() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "只有净值标才能垫付!"));
        }

        Long advanceUserId = 22L;//垫付账号
        Asset advanceUserAsses = assetService.findByUserIdLock(advanceUserId);

        Specification<BorrowRepayment> brs = null;
        int order = borrowRepayment.getOrder();
        if (order > 0) {
            brs = Specifications
                    .<BorrowRepayment>and()
                    .eq("borrowId", borrowRepayment.getBorrowId())
                    .predicate(new LtSpecification("order", new DataObject(order)))
                    .eq("status", 0)
                    .build();
            if (borrowRepaymentService.count(brs) > 0) {
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "该借款上一期还未还，请先把上一期的还上!"));
            }
        }

        long lateInterest = 0;//逾期利息
        int lateDays = 0;//逾期天数
        int diffDay = DateHelper.diffInDays(DateHelper.beginOfDate(borrowRepayment.getRepayAt()), DateHelper.beginOfDate(new Date()), false);
        if (diffDay > 0) {
            lateDays = diffDay;
            int overPrincipal = borrowRepayment.getPrincipal();//剩余未还本金
            if (order < (borrow.getTotalOrder() - 1)) {
                brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrow.getId())
                        .eq("status", 0)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                for (BorrowRepayment tempBorrowRepayment : borrowRepaymentList) {
                    overPrincipal += tempBorrowRepayment.getPrincipal();
                }
            }
            lateInterest = Math.round(overPrincipal * 0.004 * lateDays);
        }

        long repayInterest = borrowRepayment.getInterest();//还款利息
        long repayMoney = borrowRepayment.getPrincipal() + repayInterest;//还款金额
        if (advanceUserAsses.getUseMoney() < (repayMoney + lateInterest)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户余额不足，请先充值"));
        }

        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setUserId(advanceUserId);
        entity.setType(CapitalChangeEnum.ExpenditureOther);
        entity.setMoney((int) (repayMoney + lateInterest));
        entity.setRemark("对借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]第" + (order + 1) + "期的垫付还款");
        capitalChangeHelper.capitalChange(entity);

        receivedReapy(borrow, order, 1, lateDays, (int) (lateInterest / 2), true);//还款

        AdvanceLog advanceLog = new AdvanceLog();
        advanceLog.setUserId(advanceUserId);
        advanceLog.setRepaymentId(repaymentId);
        advanceLog.setAdvanceAtYes(new Date());
        advanceLog.setAdvanceMoneyYes((int) (repayMoney + lateInterest));
        advanceLogService.insert(advanceLog);

        borrowRepayment.setLateDays(lateDays);
        borrowRepayment.setLateInterest((int) lateInterest);
        borrowRepayment.setAdvanceAtYes(new Date());
        borrowRepayment.setAdvanceMoneyYes((int) (repayMoney + lateInterest));
        borrowRepaymentService.updateById(borrowRepayment);

        return ResponseEntity.ok(VoBaseResp.ok("垫付成功!"));
    }
}
