package com.gofobao.framework.repayment.biz.Impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeReq;
import com.gofobao.framework.api.model.batch_bail_repay.BailRepay;
import com.gofobao.framework.api.model.batch_bail_repay.BailRepayRun;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayReq;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayResp;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayReq;
import com.gofobao.framework.api.model.batch_repay.BatchRepayResp;
import com.gofobao.framework.api.model.batch_repay.Repay;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailReq;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailResp;
import com.gofobao.framework.api.model.batch_repay_bail.RepayBail;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.entity.AdvanceLog;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AdvanceLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoCancelBorrow;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.JixinContants;
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
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.helper.project.IntegralChangeHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.repayment.vo.response.RepayCollectionLog;
import com.gofobao.framework.repayment.vo.response.RepaymentOrderDetail;
import com.gofobao.framework.repayment.vo.response.VoViewRepayCollectionLogWarpRes;
import com.gofobao.framework.repayment.vo.response.VoViewRepaymentOrderDetailWarpRes;
import com.gofobao.framework.repayment.vo.response.pc.VoCollection;
import com.gofobao.framework.repayment.vo.response.pc.VoOrdersList;
import com.gofobao.framework.repayment.vo.response.pc.VoViewCollectionWarpRes;
import com.gofobao.framework.repayment.vo.response.pc.VoViewOrderListWarpRes;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.contants.ThirdBatchNoTypeContant;
import com.gofobao.framework.system.entity.*;
import com.gofobao.framework.system.service.DictItemServcie;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.response.VoAutoTenderInfo;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private AdvanceLogService advanceLogService;
    @Autowired
    private BorrowRepository borrowRepository;
    @Autowired
    private DictItemServcie dictItemServcie;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private DictValueService dictValueService;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    LoadingCache<String, DictValue> jixinCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemServcie.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);
                    if (ObjectUtils.isEmpty(dictItem)) {
                        return null;
                    }

                    return dictValueService.findTopByItemIdAndValue01(dictItem.getId(), bankName);
                }
            });
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private JixinManager jixinManager;


    @Override
    public ResponseEntity<VoViewCollectionDaysWarpRes> days(Long userId, String time) {
        VoViewCollectionDaysWarpRes collectionDayWarpRes = VoBaseResp.ok("查询成功", VoViewCollectionDaysWarpRes.class);
        try {
            List<Integer> result = borrowRepaymentService.days(userId, time);
            collectionDayWarpRes.setWarpRes(result);
            return ResponseEntity.ok(collectionDayWarpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionDaysWarpRes.class));

        }
    }

    /**
     * 还款计划
     *
     * @param voCollectionOrderReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewCollectionOrderListWarpResp> repaymentList(VoCollectionOrderReq voCollectionOrderReq) {
        try {
            List<BorrowRepayment> repaymentList = borrowRepaymentService.repaymentList(voCollectionOrderReq);
            if (CollectionUtils.isEmpty(repaymentList)) {
                VoViewCollectionOrderListWarpResp response = VoBaseResp.ok("查询成功", VoViewCollectionOrderListWarpResp.class);
                response.setOrder(0);
                response.setSumCollectionMoneyYes("0");
                return ResponseEntity.ok(response);
            }

            Set<Long> borrowIdSet = repaymentList.stream()
                    .map(p -> p.getBorrowId())
                    .collect(Collectors.toSet());

            List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList(borrowIdSet));
            Map<Long, Borrow> borrowMap = borrowList.stream()
                    .collect(Collectors.toMap(Borrow::getId, Function.identity()));

            List<VoViewCollectionOrderListWarpResp> orderListRes = new ArrayList<>(0);
            List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();

            repaymentList.stream().forEach(p -> {
                VoViewCollectionOrderRes collectionOrderRes = new VoViewCollectionOrderRes();
                Borrow borrow = borrowMap.get(p.getBorrowId());
                collectionOrderRes.setCollectionId(p.getId());
                collectionOrderRes.setBorrowName(borrow.getName());
                collectionOrderRes.setOrder(p.getOrder() + 1);
                collectionOrderRes.setCollectionMoneyYes(StringHelper.formatMon(p.getRepayMoneyYes() / 100d));
                collectionOrderRes.setCollectionMoney(StringHelper.formatMon(p.getRepayMoney() / 100d));
                collectionOrderRes.setTimeLime(borrow.getTimeLimit());
                orderResList.add(collectionOrderRes);
            });

            VoViewCollectionOrderListWarpResp collectionOrder = VoBaseResp.ok("查询成功", VoViewCollectionOrderListWarpResp.class);
            collectionOrder.setOrderResList(orderResList);
            //总数
            collectionOrder.setOrder(orderResList.size());
            //已还款
            Integer moneyYesSum = repaymentList.stream()
                    .filter(p -> p.getStatus() == 1)
                    .mapToInt(w -> w.getRepayMoneyYes())
                    .sum();
            collectionOrder.setSumCollectionMoneyYes(StringHelper.formatMon(moneyYesSum / 100d));
            orderListRes.add(collectionOrder);
            return ResponseEntity.ok(collectionOrder);

        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionOrderListWarpResp.class));
        }
    }

    /**
     * pc:还款计划
     *
     * @param listReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewOrderListWarpRes> pcRepaymentList(VoOrderListReq listReq) {
        try {
            VoViewOrderListWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewOrderListWarpRes.class);
            Map<String, Object> resultMaps = borrowRepaymentService.pcOrderList(listReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoOrdersList> orderList = (List<VoOrdersList>) resultMaps.get("orderList");
            warpRes.setTotalCount(totalCount);
            warpRes.setOrdersLists(orderList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewOrderListWarpRes.class));
        }
    }

    /**
     * 还款详情
     *
     * @param voInfoReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewRepaymentOrderDetailWarpRes> detail(VoInfoReq voInfoReq) {
        try {
            RepaymentOrderDetail voViewOrderDetailResp = borrowRepaymentService.detail(voInfoReq);
            VoViewRepaymentOrderDetailWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewRepaymentOrderDetailWarpRes.class);
            warpRes.setRepaymentOrderDetail(voViewOrderDetailResp);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewRepaymentOrderDetailWarpRes.class));
        }
    }


    /**
     * pc:未还款详情
     *
     * @param collectionListReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewCollectionWarpRes> orderList(VoCollectionListReq collectionListReq) {
        try {

            VoViewCollectionWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewCollectionWarpRes.class);
            Map<String, Object> resultMaps = borrowRepaymentService.collectionList(collectionListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoCollection> repaymentList = (List<VoCollection>) resultMaps.get("repaymentList");
            warpRes.setTotalCount(totalCount);
            warpRes.setVoCollections(repaymentList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewRepayCollectionLogWarpRes> logs(Long borrowId) {
        try {
            List<RepayCollectionLog> logList = borrowRepaymentService.logs(borrowId);
            VoViewRepayCollectionLogWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewRepayCollectionLogWarpRes.class);
            warpRes.setCollectionLogs(logList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewRepayCollectionLogWarpRes.class));
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
        interestPercent = (ObjectUtils.isEmpty(interestPercent) || interestPercent == 0) ? 1 : interestPercent;
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
        Asset borrowUserAsset = assetService.findByUserIdLock(borrowUserId);
        Preconditions.checkNotNull(borrowRepayment, "用户资产查询失败!");
        if ((!ObjectUtils.isEmpty(userId))
                && (!StringHelper.toString(borrowUserId).equals(StringHelper.toString(userId)))) {   // 存在userId时 判断是否是当前用户
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("操作用户不是借款用户!")));
        }

        //===================================================================
        //检查还款账户是否完成存管操作  与  完成必需操作
        //===================================================================
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "还款会员未开户！", VoAutoTenderInfo.class));
        }

        if (userThirdAccount.getPasswordState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_INIT_BANK_PASSWORD, "请初始化江西银行存管账户密码！", VoAutoTenderInfo.class));
        }

        if (userThirdAccount.getAutoTransferState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动债权转让协议！", VoAutoTenderInfo.class));
        }


        if (userThirdAccount.getAutoTenderState() != 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR_CREDIT, "请先签订自动投标协议！", VoAutoTenderInfo.class));
        }


        int repayInterest = (int) (borrowRepayment.getInterest() * interestPercent);//还款利息
        int repayMoney = borrowRepayment.getPrincipal() + repayInterest;//还款金额

        if (borrowType == 2) { // 秒表处理
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
                    .eq("id", repaymentId)
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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> repayDeal(VoRepayReq voRepayReq) throws Exception {


        ResponseEntity resp = checkRepay(voRepayReq);
        if (!ObjectUtils.isEmpty(resp)) {
            return null;
        }
        Date nowDate = new Date();
        int lateInterest = 0;//逾期利息
        Double interestPercent = voRepayReq.getInterestPercent();
        Long repaymentId = voRepayReq.getRepaymentId();
        Boolean isUserOpen = voRepayReq.getIsUserOpen();//是否是用户主动还款
        interestPercent = interestPercent == 0 ? 1 : interestPercent;//回款 利息百分比
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);//还款记录
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());//借款记录

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
        } else if (interestPercent < 1) {
            entity.setRemark("（提前结清）");
        } else if (!isUserOpen) {
            entity.setRemark("（系统自动还款）");
        }
        try {
            capitalChangeHelper.capitalChange(entity);
        } catch (Throwable e) {
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
        } catch (Throwable e) {
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
            AdvanceLog advanceLog = advanceLogService.findByRepaymentId(repaymentId);
            Preconditions.checkNotNull(advanceLog, "垫付记录不存在!请联系客服");

            entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.IncomeOther);
            entity.setUserId(advanceLog.getUserId());
            entity.setMoney(repayMoney + lateInterest);
            entity.setRemark("收到客户对借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]第" + (borrowRepayment.getOrder() + 1) + "期垫付的还款");
            capitalChangeHelper.capitalChange(entity);
            //更新垫付记录
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

        //====================================================================
        //结束债权：最后一期还款时
        //====================================================================
        if (borrow.getTotalOrder() == (borrowRepayment.getOrder() + 1)) {
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
            mqConfig.setTag(MqTagEnum.END_CREDIT);
            mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 1));
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("repaymentBizImpl repayDeal send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("repaymentBizImpl repayDeal send mq exception", e);
            }
        }

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
        } catch (Throwable e) {
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

                UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());

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
                                Math.max(DateHelper.diffInDays(endAt, startAtYes, false), 0) /
                                DateHelper.diffInDays(collectionAt, startAt, false));

                        if (accruedInterest > 0) {
                            CapitalChangeEntity entity = new CapitalChangeEntity();
                            entity.setType(CapitalChangeEnum.IncomeOther);
                            entity.setUserId(tender.getUserId());
                            entity.setMoney(accruedInterest);
                            entity.setRemark("收到借款标[" + BorrowHelper.getBorrowLink(tempBorrow.getId(), tempBorrow.getName()) + "]转让当期应计利息。");
                            capitalChangeHelper.capitalChange(entity);

                            //通过红包账户发放
                            //调用即信发放债权转让人应收利息
                            //查询红包账户
                            DictValue dictValue = jixinCache.get(JixinContants.RED_PACKET_USER_ID);
                            UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));

                            VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                            voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                            voucherPayRequest.setTxAmount(StringHelper.formatDouble(accruedInterest * 0.9, 100, false));//扣除手续费
                            voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
                            voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                            voucherPayRequest.setDesLine("发放债权转让人应收利息");
                            voucherPayRequest.setChannel(ChannelContant.HTML);
                            voucherPayRequest.setAuthCode(tender.getAuthCode());
                            VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                            if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                                String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                                log.error("BorrowRepaymentThirdBizImpl 调用即信发送发放债权转让人应收利息异常:" + msg);
                            }


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
                            interest * Math.max(DateHelper.diffInDays(endAt, startAtYes, false), 0) /
                                    DateHelper.diffInDays(collectionAt, startAt, false)
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
                        entity.setRemark("收到借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]利息管理费");
                        capitalChangeHelper.capitalChange(entity);
                    }
                }

                //逾期收入
                if ((lateDays > 0) && (lateInterest > 0)) {
                    int tempLateInterest = Math.round(tender.getValidMoney() / borrow.getMoney() * lateInterest);
                    String remark = "收到借款标[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的逾期罚息";

                    //调用即信发送红包接口
                    //查询红包账户
                    DictValue dictValue = jixinCache.get(JixinContants.RED_PACKET_USER_ID);
                    UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));

                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                    voucherPayRequest.setTxAmount(StringHelper.formatDouble(tempLateInterest, 100, false));
                    voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    voucherPayRequest.setDesLine(remark);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        throw new Exception("逾期收入发送异常：" + msg);
                    }

                    entity = new CapitalChangeEntity();
                    entity.setType(CapitalChangeEnum.IncomeOverdue);
                    entity.setUserId(tender.getUserId());
                    entity.setToUserId(borrow.getUserId());
                    entity.setMoney(tempLateInterest);
                    entity.setRemark(remark);
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
                    } catch (Throwable e) {
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
     * @param voRepayReq
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> repay(VoRepayReq voRepayReq) throws Exception {

        // ====================================
        //  1. 平台可用用金额
        //  2. 存管账户是否够用
        //  3. 冻结还款
        //  4. 还款
        // ===================================
        ResponseEntity<VoBaseResp> resp = checkRepay(voRepayReq);
        if (!ObjectUtils.isEmpty(resp)) {
            return resp;
        }
        VoThirdBatchRepay voThirdBatchRepay = new VoThirdBatchRepay();
        voThirdBatchRepay.setUserId(voRepayReq.getUserId());
        voThirdBatchRepay.setRepaymentId(voRepayReq.getRepaymentId());
        voThirdBatchRepay.setInterestPercent(0d);
        voThirdBatchRepay.setIsUserOpen(true);

        // ====================================
        // 存管第三方还款操作
        // ====================================
        Date nowDate = new Date();
        Long repaymentId = voThirdBatchRepay.getRepaymentId();
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Preconditions.checkNotNull(borrowRepayment, "还款不存在!");
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrowRepayment.getUserId());
        Preconditions.checkNotNull(borrowUserThirdAccount, "借款人未开户!");

        List<Repay> repayList = null;
        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
            repayList = borrowRepaymentThirdBiz.getRepayList(voThirdBatchRepay);
        } else {
            //批次融资人还担保账户垫款
            VoBatchRepayBailReq voBatchRepayBailReq = new VoBatchRepayBailReq();
            voBatchRepayBailReq.setRepaymentId(repaymentId);
            voBatchRepayBailReq.setInterestPercent(voBatchRepayBailReq.getInterestPercent());
            voBatchRepayBailReq.setUserId(voBatchRepayBailReq.getUserId());
            voBatchRepayBailReq.setIsUserOpen(voBatchRepayBailReq.getIsUserOpen());
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

        //====================================================================
        //冻结借款人账户资金
        //====================================================================
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(borrowUserThirdAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(txAmount, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, balanceFreezeReq, BatchDetailsQueryResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(batchDetailsQueryResp.getRetCode()))) {
            throw new Exception("即信批次还款冻结资金失败：" + batchDetailsQueryResp.getRetMsg());
        }

        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repayDeal/run");
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repayDeal/check");
        request.setAcqRes(GSON.toJson(voThirdBatchRepay));
        request.setSubPacks(GSON.toJson(repayList));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(repayList.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            throw new Exception("即信批次还款失败：" + response.getRetMsg());
        }
        return ResponseEntity.ok(VoBaseResp.ok("还款成功"));
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
                    .eq("status", 1)
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

                txAmount = principal + intAmount + txFeeOut;

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

                borrowCollection.setTRepayBailOrderId(orderId);
                borrowCollectionService.updateById(borrowCollection);
            }
        } while (false);
    }

    /**
     * 立即还款
     *
     * @param voPcInstantlyRepaymentReq
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> pcRepay(VoPcInstantlyRepaymentReq voPcInstantlyRepaymentReq) throws Exception {

        String paramStr = voPcInstantlyRepaymentReq.getParamStr();
        if (!SecurityHelper.checkSign(voPcInstantlyRepaymentReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long repaymentId = NumberHelper.toLong(paramMap.get("repaymentId"));
        BorrowRepayment borrowRepayment = borrowRepaymentService.findById(repaymentId);

        VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setRepaymentId(repaymentId);
        voRepayReq.setUserId(borrowRepayment.getUserId());
        return repay(voRepayReq);
    }

    /**
     * 垫付检查
     *
     * @param repaymentId
     * @return
     */
    private ResponseEntity<VoBaseResp> advanceCheck(Long repaymentId) throws Exception {
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
        int diffDay = DateHelper.diffInDays(DateHelper.beginOfDate(new Date()), DateHelper.beginOfDate(borrowRepayment.getRepayAt()), false);
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

        return null;
    }

    /**
     * pc垫付
     *
     * @param voPcAdvanceReq
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoBaseResp> pcAdvance(VoPcAdvanceReq voPcAdvanceReq) throws Exception {
        String paramStr = voPcAdvanceReq.getParamStr();
        if (!SecurityHelper.checkSign(voPcAdvanceReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long repaymentId = NumberHelper.toLong(paramMap.get("repaymentId"));

        VoAdvanceReq voAdvanceReq = new VoAdvanceReq();
        voAdvanceReq.setRepaymentId(repaymentId);
        return advance(voAdvanceReq);
    }

    /**
     * 垫付
     *
     * @param voAdvanceReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> advance(VoAdvanceReq voAdvanceReq) throws Exception {
        Long repaymentId = voAdvanceReq.getRepaymentId();

        ResponseEntity resp = advanceCheck(repaymentId);
        if (!ObjectUtils.isEmpty(resp)) {
            return resp;
        }
        VoBatchBailRepayReq voBatchBailRepayReq = new VoBatchBailRepayReq();
        voBatchBailRepayReq.setRepaymentId(repaymentId);

        //=======================================================
        // 调用存管担保人代偿
        //=======================================================
        Date nowDate = new Date();

        List<BailRepay> bailRepayList = borrowRepaymentThirdBiz.getBailRepayList(voBatchBailRepayReq);
        if (CollectionUtils.isEmpty(bailRepayList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "代偿不存在"));
        }

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());
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
        thirdBatchLog.setType(ThirdBatchNoTypeContant.BAIL_REPAY);
        thirdBatchLog.setRemark("即信担保人还垫付");
        thirdBatchLogService.save(thirdBatchLog);

        //====================================================================
        //冻结借款人账户资金
        //====================================================================
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(borrowUserThirdAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(txAmount, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BatchDetailsQueryResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(batchDetailsQueryResp.getRetCode()))) {
            throw new Exception("即信批次还款冻结资金失败：" + batchDetailsQueryResp.getRetMsg());
        }

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
     * 垫付处理
     *
     * @param voAdvanceReq
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> advanceDeal(VoAdvanceCall voAdvanceReq) throws Exception {

        ResponseEntity resp = advanceCheck(voAdvanceReq.getRepaymentId());//垫付检查
        if (!ObjectUtils.isEmpty(resp)) {
            return resp;
        }
        Long repaymentId = voAdvanceReq.getRepaymentId();
        List<BailRepayRun> bailRepayRunList = voAdvanceReq.getBailRepayRunList();
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());

        Long advanceUserId = 22L;//垫付账号
        Asset advanceUserAsses = assetService.findByUserIdLock(advanceUserId);

        Specification<BorrowRepayment> brs = null;
        int order = borrowRepayment.getOrder();

        long lateInterest = 0;//逾期利息
        int lateDays = 0;//逾期天数
        int diffDay = DateHelper.diffInDays(DateHelper.beginOfDate(new Date()), DateHelper.beginOfDate(borrowRepayment.getRepayAt()), false);
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

        //存储即信授权码
        List<String> orderList = new ArrayList<>();
        for (BailRepayRun bailRepayRun : bailRepayRunList) {
            orderList.add(bailRepayRun.getOrderId());
        }

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("tBailRepayOrderId", orderList.toArray())
                .build();

        int pageIndex = 0;
        int maxPageSize = 50;
        Pageable pageable = null;
        List<BorrowCollection> borrowCollectionList = null;
        do {
            pageable = new PageRequest(pageIndex++, maxPageSize, new Sort(Sort.Direction.ASC, "id"));
            borrowCollectionList = borrowCollectionService.findList(bcs, pageable);
            if (CollectionUtils.isEmpty(borrowCollectionList)) {
                break;
            }
            for (BorrowCollection borrowCollection : borrowCollectionList) {
                for (BailRepayRun bailRepayRun : bailRepayRunList) {
                    if (borrowCollection.getTBailRepayOrderId().equals(bailRepayRun.getOrderId())) {
                        borrowCollection.setTBailAuthCode(bailRepayRun.getAuthCode());
                        break;
                    }
                }
            }
            borrowCollectionService.save(borrowCollectionList);
        } while (borrowCollectionList.size() >= maxPageSize);
        return ResponseEntity.ok(VoBaseResp.ok("垫付成功!"));
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
        if (!ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) {
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

        //记录日志
        String batchNo = jixinHelper.getBatchNo();
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchNoTypeContant.REPAY_BAIL);
        thirdBatchLog.setRemark("批次融资人还担保账户垫款");
        thirdBatchLogService.save(thirdBatchLog);

        //====================================================================
        //冻结担保人账户资金
        //====================================================================
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(borrow.getBailAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(txAmount, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BatchDetailsQueryResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(batchDetailsQueryResp.getRetCode()))) {
            throw new Exception("即信批次还款冻结资金失败：" + batchDetailsQueryResp.getRetMsg());
        }


        BatchRepayBailReq request = new BatchRepayBailReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setSubPacks(GSON.toJson(repayBails));
        request.setTxCounts(StringHelper.toString(repayBails.size()));
        request.setNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repaybail/check");
        request.setRetNotifyURL(webDomain + "/pub/repayment/v2/third/batch/repaybail/run");
        request.setAcqRes(GSON.toJson(voBatchRepayBailReq));
        request.setChannel(ChannelContant.HTML);
        BatchRepayBailResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY_BAIL, request, BatchRepayBailResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次融资人还担保账户垫款失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("批次融资人还担保账户垫款成功!"));
    }
}
