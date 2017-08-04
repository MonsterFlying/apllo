package com.gofobao.framework.repayment.biz.Impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeReq;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeResp;
import com.gofobao.framework.api.model.batch_bail_repay.BailRepay;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayReq;
import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayReq;
import com.gofobao.framework.api.model.batch_repay.BatchRepayResp;
import com.gofobao.framework.api.model.batch_repay.Repay;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailReq;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailResp;
import com.gofobao.framework.api.model.batch_repay_bail.RepayBail;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.AdvanceLog;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.AdvanceLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
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
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.JixinContants;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LtSpecification;
import com.gofobao.framework.common.integral.IntegralChangeEntity;
import com.gofobao.framework.common.integral.IntegralChangeEnum;
import com.gofobao.framework.common.jxl.ExcelException;
import com.gofobao.framework.common.jxl.ExcelUtil;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.*;
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
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.*;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.tender.vo.request.VoEndTransfer;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletResponse;
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
    private DictItemService dictItemService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private DictValueService dictValueService;
    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private BatchAssetChangeHelper batchAssetChangeHelper;
    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;
    @Autowired
    private AssetChangeProvider assetChangeProvider;
    @Autowired
    private TransferService transferService;
    @Autowired
    private TransferBiz transferBiz;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;


    LoadingCache<String, DictValue> jixinCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, DictValue>() {
                @Override
                public DictValue load(String bankName) throws Exception {
                    DictItem dictItem = dictItemService.findTopByAliasCodeAndDel("JIXIN_PARAM", 0);
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
            long moneyYesSum = repaymentList.stream()
                    .filter(p -> p.getStatus() == 1)
                    .mapToLong(w -> w.getRepayMoneyYes())
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

    @Override
    public void toExcel(HttpServletResponse response, VoOrderListReq listReq) {

        List<VoOrdersList> ordersLists = borrowRepaymentService.toExcel(listReq);
        if (!CollectionUtils.isEmpty(ordersLists)) {
            LinkedHashMap<String, String> paramMaps = Maps.newLinkedHashMap();
            paramMaps.put("time", "时间");
            paramMaps.put("collectionMoney", "本息");
            paramMaps.put("principal", "本金");
            paramMaps.put("interest", "利息");
            paramMaps.put("orderCount", "笔数");
            try {
                ExcelUtil.listToExcel(ordersLists, paramMaps, "还款计划", response);
            } catch (ExcelException e) {
                e.printStackTrace();
            }
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
     * 前置判断
     *
     * @param voRepayReq
     * @return
     */
    private ResponseEntity<VoBaseResp> checkRepay(VoRepayReq voRepayReq) {
        /* 逾期利息 */
        int lateInterest = 0;
        Long userId = voRepayReq.getUserId();
        Long repaymentId = voRepayReq.getRepaymentId();
        /* 计息百分比 */
        Double interestPercent = voRepayReq.getInterestPercent();
        interestPercent = ObjectUtils.isEmpty(interestPercent) ? 1 : interestPercent;

        //查询还款记录 并且判断是否还款
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Preconditions.checkNotNull(borrowRepayment, "还款不存在!");
        if (borrowRepayment.getStatus() != 0) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("还款状态已发生改变!")));
        }

        //查询当前还款的借款信息
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        Preconditions.checkNotNull(borrow, "借款记录不存在!");
        int borrowType = borrow.getType();//借款type
        long borrowUserId = borrow.getUserId();

        Asset borrowUserAsset = assetService.findByUserIdLock(borrowUserId);
        Preconditions.checkNotNull(borrowRepayment, "用户资产查询失败!");


        if ((!ObjectUtils.isEmpty(userId))
                && (!StringHelper.toString(borrowUserId).equals(StringHelper.toString(userId)))) {   // 存在userId时 判断是否是当前用户
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("操作用户不是借款用户!")));
        }

        //===================================================================
        //检查还款账户是否完成存管操作  与  完成必需操作
        //===================================================================
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        int repayInterest = (int) (borrowRepayment.getInterest() * interestPercent); //还款利息
        long repayMoney = borrowRepayment.getPrincipal() + repayInterest;//还款金额

        if (borrowType == 2) { // 秒标处理
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

        //判断提交还款批次是否多次重复提交
        int flag = thirdBatchLogBiz.checkBatchOftenSubmit(String.valueOf(repaymentId), ThirdBatchLogContants.BATCH_REPAY_BAIL, ThirdBatchLogContants.BATCH_REPAY);
        if (flag == ThirdBatchLogContants.AWAIT) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("还款处理中，请勿重复点击!")));
        } else if (flag == ThirdBatchLogContants.SUCCESS) {
            //批次放款队列参数
            Map<String, Object> acqResMap = new HashMap<>();
            acqResMap.put("userId", userId);
            acqResMap.put("repaymentId", repaymentId);
            acqResMap.put("interestPercent", 1d);
            acqResMap.put("isUserOpen", true);

            //获取最后一条有效的发布批次记录
            ThirdBatchLog thirdBatchLog = thirdBatchLogBiz.getValidLastBatchLog(StringHelper.toString(repaymentId), ThirdBatchLogContants.BATCH_REPAY_BAIL, ThirdBatchLogContants.BATCH_REPAY);

            //触发处理批次放款处理结果队列
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
            mqConfig.setTag(MqTagEnum.BATCH_DEAL);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.SOURCE_ID, StringHelper.toString(repaymentId),
                            MqConfig.ACQ_RES, GSON.toJson(acqResMap),
                            MqConfig.BATCH_NO, StringHelper.toString(thirdBatchLog.getBatchNo()),
                            MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("tenderThirdBizImpl thirdBatchRepayRunCall send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("tenderThirdBizImpl thirdBatchRepayRunCall send mq exception", e);
            }
        }

        //判断这个借款上一期是否归还
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
        return ResponseEntity.ok(VoBaseResp.ok("验证成功!"));
    }

    /**
     * 新还款处理
     * 1.查询并判断还款记录是否存在!
     * 2.处理资金还款人、收款人资金变动
     * 3.判断是否是还担保人垫付，垫付需要改变垫付记录状态
     * 4.还款成功后变更改还款状态
     * 5.结束债权
     * 6.发送投资人收到还款站内信
     * 7.投资人收到积分
     * 8.还款最后新增统计
     *
     * @param repaymentId
     * @return
     * @throws Exception
     */
    public ResponseEntity<VoBaseResp> newRepayDeal(long repaymentId, long batchNo) throws Exception {
        //1.查询并判断还款记录是否存在!
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);/* 当期还款记录 */
        Preconditions.checkNotNull(borrowRepayment, "还款记录不存在!");
        Borrow parentBorrow = borrowService.findById(borrowRepayment.getBorrowId());/* 还款记录对应的借款记录 */
        Preconditions.checkNotNull(parentBorrow, "借款记录不存在!");
        /* 还款对应的投标记录  包括债权转让在里面 */
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", parentBorrow.getId())
                .build();
        List<Tender> tenderList = tenderService.findList(ts);/* 还款对应的投标记录  包括债权转让在里面 */
        Preconditions.checkNotNull(tenderList, "立即还款: 投标记录为空!");
        /* 投标记录id */
        Set<Long> tenderIds = tenderList.stream().map(tender -> tender.getId()).collect(Collectors.toSet());
        /* 查询未转让的投标记录回款记录 */
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("order", borrowRepayment.getOrder())
                .eq("transferFlag", 0)
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Preconditions.checkNotNull(borrowCollectionList, "立即还款: 回款记录为空!");
        /* 是否垫付 */
        boolean advance = ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes());
        //2.处理资金还款人、收款人资金变动
        batchAssetChangeHelper.batchAssetChange(repaymentId, batchNo, BatchAssetChangeContants.BATCH_LEND_REPAY);
        //3.判断是否是还担保人垫付，垫付需要改变垫付记录状态
        //4.还款成功后变更改还款状态
        changeRepaymentAndAdvanceStatus(borrowRepayment);
        //5.结束第三方债权并更新借款状态（还款最后一期的时候）
        endThirdTenderAndChangeBorrowStatus(parentBorrow, borrowRepayment);
        //6.发送投资人收到还款站内信
        sendCollectionNotices(borrowCollectionList, advance, parentBorrow);
        //7.发放积分
        giveInterest(borrowCollectionList, parentBorrow);
        //8.还款最后新增统计
        updateRepaymentStatistics(parentBorrow, borrowRepayment);
        /**
         * //updateUserCacheByReceivedRepay(borrowCollection, tender, borrow);
         //项目回款短信通知
         //smsNoticeByReceivedRepay(borrowCollection, tender, borrow);
         */
        return ResponseEntity.ok(VoBaseResp.ok("还款处理成功!"));
    }

    /**
     * 给投资人发放积分
     *
     * @param borrowCollectionList
     * @param parentBorrow
     */
    private void giveInterest(List<BorrowCollection> borrowCollectionList, Borrow parentBorrow) {
        borrowCollectionList.stream().forEach(borrowCollection -> {
            long actualInterest = borrowCollection.getCollectionMoneyYes() - borrowCollection.getPrincipal();/* 实收利息 */
            //投资积分
            long integral = actualInterest / 100 * 10;
            if ((parentBorrow.getType() == 0 || parentBorrow.getType() == 4) && 0 < integral) {
                IntegralChangeEntity integralChangeEntity = new IntegralChangeEntity();
                integralChangeEntity.setType(IntegralChangeEnum.TENDER);
                integralChangeEntity.setValue(integral);
                integralChangeEntity.setUserId(borrowCollection.getUserId());
                try {
                    integralChangeHelper.integralChange(integralChangeEntity);
                } catch (Exception e) {
                    log.error("投资人回款积分发放失败：", e);
                }
            }
        });
    }

    /**
     * 发送回款站内信
     *
     * @param borrowCollectionList
     * @param advance
     * @param parentBorrow
     */
    private void sendCollectionNotices(List<BorrowCollection> borrowCollectionList, boolean advance, Borrow parentBorrow) {

        //迭代投标人记录
        borrowCollectionList.stream().forEach(borrowCollection -> {
            long actualInterest = borrowCollection.getCollectionMoneyYes() - borrowCollection.getPrincipal();/* 实收利息 */
            String noticeContent = String.format("客户在%s已将借款[%s]第%s期还款,还款金额为%s元", DateHelper.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"), BorrowHelper.getBorrowLink(parentBorrow.getId()
                    , parentBorrow.getName()), (borrowCollection.getOrder() + 1), StringHelper.formatDouble(actualInterest, 100, true));
            if (advance) {
                noticeContent = "广富宝在" + DateHelper.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss") + " 已将借款[" + BorrowHelper.getBorrowLink(parentBorrow.getId(), parentBorrow.getName()) +
                        "]第" + (borrowCollection.getOrder() + 1) + "期垫付还款,垫付金额为" + StringHelper.formatDouble(actualInterest, 100, true) + "元";
            }

            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(borrowCollection.getUserId());
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
                log.info(String.format("repaymentBizImpl sendCollectionNotices send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("repaymentBizImpl sendCollectionNotices send mq exception", e);
            }
        });
    }

    /**
     * 还款最后新增统计
     *
     * @param borrowRepayment
     */
    private void updateRepaymentStatistics(Borrow parentBorrow, BorrowRepayment borrowRepayment) {
        //更新统计数据
        try {
            long repayMoney = borrowRepayment.getRepayMoney();/* 还款金额 */
            long principal = borrowRepayment.getPrincipal();/* 还款本金 */
            Statistic statistic = new Statistic();
            statistic.setWaitRepayTotal(-repayMoney);
            if (!parentBorrow.isTransfer()) {//判断非转让标
                if (parentBorrow.getType() == 0) { //车贷标
                    statistic.setTjWaitRepayPrincipalTotal(-principal);
                    statistic.setTjWaitRepayTotal(-repayMoney);
                } else if (parentBorrow.getType() == 1) { //净值标
                    statistic.setJzWaitRepayPrincipalTotal(-principal);
                    statistic.setJzWaitRepayTotal(-repayMoney);
                } else if (parentBorrow.getType() == 4) { //渠道标
                    statistic.setQdWaitRepayPrincipalTotal(-principal);
                    statistic.setQdWaitRepayTotal(-repayMoney);
                }
            }
            if (!ObjectUtils.isEmpty(statistic)) {
                statisticBiz.caculate(statistic);
            }
        } catch (Throwable e) {
            log.error(String.format("repaymentBizImpl updateRepaymentStatistics 立即还款统计错误：", e));
        }
    }

    /**
     * 结束第三方债权并更新借款状态（还款最后一期的时候）
     *
     * @param borrowRepayment
     */
    private void endThirdTenderAndChangeBorrowStatus(Borrow parentBorrow, BorrowRepayment borrowRepayment) {
        //结束债权：最后一期还款时
        if (borrowRepayment.getOrder() == (parentBorrow.getTotalOrder() - 1)) {
            parentBorrow.setCloseAt(borrowRepayment.getRepayAtYes());

            //推送队列结束债权
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
            mqConfig.setTag(MqTagEnum.END_CREDIT_BY_NOT_TRANSFER);
            mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 1));
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(parentBorrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("repaymentBizImpl endThirdTenderAndChangeBorrowStatus send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("repaymentBizImpl endThirdTenderAndChangeBorrowStatus send mq exception", e);
            }
            parentBorrow.setUpdatedAt(new Date());
            borrowService.updateById(parentBorrow);
        }
    }

    /**
     * @param borrowRepayment
     * @throws Exception
     * 3.判断是否是还担保人垫付，垫付需要改变垫付记录状态（逾期天数与日期应当在还款前计算完成）
     * 4.还款成功后变更改还款状态（还款金额在还款前计算完成）
     */
    private void changeRepaymentAndAdvanceStatus(BorrowRepayment borrowRepayment) throws Exception {
        //更改垫付记录、还款记录状态
        borrowRepayment.setStatus(1);
        borrowRepayment.setRepayAtYes(new Date());
        borrowRepaymentService.updateById(borrowRepayment);

        if (!ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) { //存在垫付时间则当条还款已经被垫付过
            AdvanceLog advanceLog = advanceLogService.findByRepaymentId(borrowRepayment.getId());
            Preconditions.checkNotNull(advanceLog, "RepaymentBizImpl changeRepaymentAndAdvanceStatus 垫付记录不存在!请联系客服。");

            //更新垫付记录转状态
            advanceLog.setStatus(1);
            advanceLog.setRepayAtYes(new Date());
            advanceLogService.updateById(advanceLog);
        }
    }

    /**
     * 立即还款
     *
     * @param voRepayReq
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> repayDeal(VoRepayReq voRepayReq) throws Exception {
        log.info("立即还款开始");
        ResponseEntity<VoBaseResp> resp = checkRepay(voRepayReq);
        if (resp.getBody().getState().getCode() != VoBaseResp.OK) {
            return resp;
        }
        Date nowDate = new Date();
        long lateInterest = 0;//逾期利息
        Double interestPercent = voRepayReq.getInterestPercent();
        Long repaymentId = voRepayReq.getRepaymentId();
        Boolean isUserOpen = voRepayReq.getIsUserOpen();//是否是用户主动还款
        interestPercent = ObjectUtils.isEmpty(interestPercent) ? 1 : interestPercent;//回款 利息百分比
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);// 还款记录
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());//借款记录

        Long borrowId = borrow.getId();//借款ID
        int borrowType = borrow.getType();//借款type
        Long borrowUserId = borrow.getUserId();
        int repayInterest = (int) (borrowRepayment.getInterest() * interestPercent);//还款利息
        long repayMoney = borrowRepayment.getPrincipal() + repayInterest;//还款金额

        //逾期天数
        int lateDays = getLateDays(borrowRepayment);
        if (0 < lateDays) {
            long overPrincipal = borrowRepayment.getPrincipal();//剩余未还本金
            if (borrowRepayment.getOrder() < (borrow.getTotalOrder() - 1)) {//计算非一次性还本付息 剩余本金
                Specification<BorrowRepayment> brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrowId)
                        .eq("status", 0)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                Preconditions.checkNotNull(borrowRepayment, "还款不存在!");
                //剩余未还金额
                overPrincipal = borrowRepaymentList.stream().mapToLong(br -> br.getPrincipal()).sum();
            }

            lateInterest = (int) MathHelper.myRound(overPrincipal * 0.004 * lateDays, 2);
        }

        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.NewRepayment);
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
        capitalChangeHelper.capitalChange(entity);

        //扣除待还
        entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.PaymentLower);
        entity.setUserId(borrowUserId);
        entity.setMoney(borrowRepayment.getRepayMoney());
        entity.setInterest(borrowRepayment.getInterest());
        entity.setRemark("还款成功扣除待还");
        capitalChangeHelper.capitalChange(entity);
        if ((lateDays > 0) && (lateInterest > 0)) {
            entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.NewOverdue);
            entity.setUserId(borrowUserId);
            entity.setMoney(lateInterest);
            entity.setRemark("借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]的逾期罚息");
            capitalChangeHelper.capitalChange(entity);
        }

        if (ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes())) { //非垫付
            receivedReapy(borrow, borrowRepayment.getOrder(), interestPercent, lateDays, lateInterest / 2, false);
        } else { //垫付
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

        //====================================================================
        //结束债权：最后一期还款时
        //====================================================================
        if (borrowRepayment.getOrder() == (borrow.getTotalOrder() - 1)) {
            borrow.setCloseAt(borrowRepayment.getRepayAtYes());

            //推送队列结束债权
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
            mqConfig.setTag(MqTagEnum.END_CREDIT_BY_NOT_TRANSFER);
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
    private void receivedReapy(Borrow borrow, int order, double interestPercent, int lateDays, long lateInterest, boolean advance) throws Exception {
        //会员用户集合
        Set<Long> collectionUserIds = new HashSet<>();
        Long borrowId = borrow.getId();

        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowId)
                .build();

        List<Tender> tenderList = tenderService.findList(specification);
        Preconditions.checkNotNull(tenderList, "立即还款: 投标记录为空!");

        Set<Long> userIds = tenderList.stream().map(tender -> tender.getUserId()).collect(Collectors.toSet());
        List<Long> tenderIds = tenderList.stream().map(tender -> tender.getId()).collect(Collectors.toList());

        Specification<UserCache> ucs = Specifications
                .<UserCache>and()
                .in("userId", userIds.toArray())
                .build();

        List<UserCache> userCacheList = userCacheService.findList(ucs);
        Preconditions.checkNotNull(userCacheList, "立即还款: 会员缓存记录为空!");

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("order", order)
                .build();

        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Preconditions.checkNotNull(userCacheList, "立即还款: 回款记录为空!");
        Map<Long /** tenderId */, BorrowCollection /** tender 对应的待收*/> borrowCollectionMap = borrowCollectionList
                .stream()
                .collect(Collectors.toMap(BorrowCollection::getTenderId, Function.identity()));

        Specification<UserThirdAccount> uta = Specifications
                .<UserThirdAccount>and()
                .in("userId", userIds.toArray())
                .build();
        List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(uta);
        Preconditions.checkNotNull(userThirdAccountList, "立即还款: 用户存管信息列表为空!");
        Map<Long /** userId */, UserThirdAccount /** tender 对应的待收*/> userThirdAccountMap = userThirdAccountList
                .stream()
                .collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));

        for (Tender tender : tenderList) {
            UserThirdAccount userThirdAccount = userThirdAccountMap.get(tender.getUserId());
            Preconditions.checkNotNull(userThirdAccount, "立即还款: 用户存管信息为空!");
            BorrowCollection borrowCollection = borrowCollectionMap.get(tender.getId());   //获取当前借款的回款记录
            Preconditions.checkNotNull(borrowCollection, "立即还款: 待还计划为空!");

            if (tender.getTransferFlag() == 1) {//转让中
                Specification<Borrow> bs = Specifications
                        .<Borrow>and()
                        .in("status", 0, 1)
                        .eq("tenderId", tender.getId())
                        .build();

                List<Borrow> borrowList = borrowService.findList(bs);
                if (!CollectionUtils.isEmpty(borrowList)) {
                    VoCancelBorrow voCancelBorrow = new VoCancelBorrow();
                    voCancelBorrow.setBorrowId(borrowList.get(0).getId());
                    //取消当前借款
                    borrowBiz.cancelBorrow(voCancelBorrow);
                }
                tender.setTransferFlag(0);//设置转让标识
            }

            if (tender.getTransferFlag() == 2) { //已转让
                Specification<Borrow> bs = Specifications
                        .<Borrow>and()
                        .eq("tenderId", tender.getId())
                        .eq("status", 3)
                        .build();

                List<Borrow> tranferedBorrowList = borrowService.findList(bs);
                if (CollectionUtils.isEmpty(tranferedBorrowList)) {
                    continue;
                }

                Borrow tranferedBorrow = tranferedBorrowList.get(0);
                int tranferedOrder = order + tranferedBorrow.getTotalOrder() - borrow.getTotalOrder();
                long tranferedLateInterest = tender.getValidMoney() / borrow.getMoney() * lateInterest;
                long accruedInterest = 0;
                if (tranferedOrder == 0) { //如果是转让后第一期回款, 则计算转让者首期应计利息
                    long interest = borrowCollection.getInterest();
                    Date startAt = DateHelper.beginOfDate(borrowCollection.getStartAt());//获取00点00分00秒
                    Date collectionAt = DateHelper.beginOfDate(borrowCollection.getCollectionAt());
                    Date startAtYes = DateHelper.beginOfDate(borrowCollection.getStartAtYes());
                    Date endAt = DateHelper.beginOfDate(tranferedBorrow.getSuccessAt());
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
                        entity.setRemark("收到借款标[" + BorrowHelper.getBorrowLink(tranferedBorrow.getId(), tranferedBorrow.getName()) + "]转让当期应计利息。");
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

                        Long integral = new Long(accruedInterest * 10);
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
                receivedReapy(tranferedBorrow, tranferedOrder, interestPercent, lateDays, tranferedLateInterest, advance);

                if (tranferedOrder == (tranferedBorrow.getTotalOrder() - 1)) {
                    tranferedBorrow.setCloseAt(borrowCollection.getCollectionAtYes());
                    borrowService.updateById(tranferedBorrow);
                }
                continue;
            }

            int collectionInterest = (int) (borrowCollection.getInterest() * interestPercent);
            long collectionMoney = borrowCollection.getPrincipal() + collectionInterest;

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
                long interest = borrowCollection.getInterest();
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
                int tempLateInterest = (int) MathHelper.myRound((double) tender.getValidMoney() / (double) borrow.getMoney() * lateInterest, 0);
                String remark = "收到借款标'" + borrow.getName() + "'的逾期罚息";

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
            long integral = (collectionInterest - interestLower) * 10;
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

            /**
             * @// TODO: 2017/7/17
             */

            //收到车贷标回款扣除 自身车贷标待收本金 和 推荐人的邀请用户车贷标总待收本金
            //updateUserCacheByReceivedRepay(borrowCollection, tender, borrow);
            //项目回款短信通知
            //smsNoticeByReceivedRepay(borrowCollection, tender, borrow);
            //事件event(new ReceivedRepay($collection, $tender, $borrow));
        }
    }

    /**
     * 新版立即还款
     * 1.还款判断
     * 2.
     *
     * @param repayReq
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public ResponseEntity<VoBaseResp> newRepay(VoRepayReq repayReq) throws Exception {
        /* 还款人id */
        long userId = repayReq.getUserId();
        /* 还款记录id */
        long borrowRepaymentId = repayReq.getRepaymentId();
        /* 利息百分比 */
        double interestPercent = repayReq.getInterestPercent();
        /* 是否是本人还款 */
        boolean isUserOpen = repayReq.getIsUserOpen();
        //还款前置判断
        ResponseEntity<VoBaseResp> resp = checkRepay(repayReq);
        if (resp.getBody().getState().getCode() != VoBaseResp.OK) {
            return resp;
        }

        UserThirdAccount repayUserThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(repayUserThirdAccount, "批量还款: 还款用户存管账户不存在");
        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(borrowRepaymentId);
        Preconditions.checkNotNull(borrowRepayment, "批量还款: 还款记录不存在");
        Borrow parentBorrow = borrowService.findByIdLock(borrowRepayment.getBorrowId());
        Preconditions.checkNotNull(parentBorrow, "批量还款: 还款标的信息不存在");
        ResponseEntity<VoBaseResp> conditionResponse = repayConditionCheck(repayUserThirdAccount, borrowRepayment);
        if (!conditionResponse.getStatusCode().equals(HttpStatus.OK)) {
            return conditionResponse;
        }
        //计算逾期天数
        int lateDays = getLateDays(borrowRepayment);
        // 计算逾期产生的总费用
        long lateInterest = calculateLateInterest(lateDays, borrowRepayment, parentBorrow);
        //是否是垫付
        boolean advance = !ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes());
        /* 批次号 */
        String batchNo = jixinHelper.getBatchNo();
        /* 资产记录流水号 */
        String seqNo = assetChangeProvider.getSeqNo();
        /* 资产记录分组流水号 */
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        //生成还款主记录
        BatchAssetChange batchAssetChange = addBatchAssetChange(batchNo, borrowRepayment.getId(), advance);
        //生成还款人还款批次资金改变记录
        addBatchAssetChangeByBorrower(batchAssetChange.getId(), borrowRepayment, parentBorrow, interestPercent, isUserOpen, lateInterest);

        if (advance) {
            //创建还款主记录
            resp = repayGuarantor(userId, repayUserThirdAccount, borrowRepayment, parentBorrow, lateInterest, batchNo);
            //生成担保人还垫付资产变更记录
            addBatchAssetChangeByGuarantor(borrowRepayment.getId(), borrowRepayment, parentBorrow, lateInterest, seqNo, groupSeqNo);
        } else {
            resp = normalRepay(userId, repayUserThirdAccount, borrowRepayment, parentBorrow, lateInterest, batchNo);
            // 生成非垫付还款生成批次资产变更记录
            addBatchAssetChangeByRepay(batchAssetChange.getId(), borrowRepayment, parentBorrow, interestPercent, lateInterest, false, seqNo, groupSeqNo);
        }

        //改变还款与垫付记录的值
        changeRepaymentAndAdvanceRecord(borrowRepayment, lateDays, lateInterest, advance);
        return resp;
    }

    /**
     * 改变还款与垫付记录的值
     *
     * @param borrowRepayment
     * @param lateDays
     * @param lateInterest
     * @param advance
     */
    public void changeRepaymentAndAdvanceRecord(BorrowRepayment borrowRepayment, int lateDays, long lateInterest, boolean advance) {
        Date nowDate = new Date();
        borrowRepayment.setLateDays(lateDays);
        borrowRepayment.setLateInterest(lateInterest);
        borrowRepayment.setRepayMoneyYes(borrowRepayment.getRepayMoney());
        borrowRepayment.setUpdatedAt(nowDate);
        borrowRepaymentService.save(borrowRepayment);
        if (advance) {
            AdvanceLog advanceLog = advanceLogService.findByRepaymentId(borrowRepayment.getId());/* 担保人还款记录 */
            Preconditions.checkNotNull(advanceLog, "垫付记录不存在!请联系客服");
            //更新垫付记录
            advanceLog.setRepayMoneyYes(borrowRepayment.getRepayMoney() + lateInterest);
            advanceLogService.updateById(advanceLog);
        }
    }


    /**
     * 新增资产更改记录
     *
     * @param batchNo
     * @param id
     * @param advance
     * @return
     */
    private BatchAssetChange addBatchAssetChange(String batchNo, Long id, boolean advance) {
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setSourceId(id);
        batchAssetChange.setState(0);
        if (advance) { //还款人还垫付
            batchAssetChange.setType(BatchAssetChangeContants.BATCH_BAIL_REPAY);
        } else { //正常还款
            batchAssetChange.setType(BatchAssetChangeContants.BATCH_REPAY);
        }
        batchAssetChange.setCreatedAt(new Date());
        batchAssetChange.setUpdatedAt(new Date());
        batchAssetChange.setBatchNo(batchNo);
        return batchAssetChange;
    }

    /**
     * 生成正常还款批次资金改变记录
     *
     * @param borrowRepayment
     * @param parentBorrow
     * @param interestPercent
     * @param lateInterest
     * @param advance
     * @throws Exception
     */
    public void addBatchAssetChangeByRepay(long batchAssetChangeId, BorrowRepayment borrowRepayment, Borrow parentBorrow,
                                           double interestPercent,
                                           long lateInterest, boolean advance,
                                           String seqNo, String groupSeqNo) throws Exception {
        Date nowDate = new Date();
        //1.直接查询投资记录
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", parentBorrow.getId())
                .build();
        List<Tender> tenderList = tenderService.findList(ts);/* 还款对应的投标记录  包括债权转让在里面 */
        Preconditions.checkNotNull(tenderList, "立即还款: 投标记录为空!");
        Set<Long> userIds = tenderList.stream().map(tender -> tender.getUserId()).collect(Collectors.toSet());/* 投标人会员id */
        Set<Long> tenderIds = tenderList.stream().map(tender -> tender.getId()).collect(Collectors.toSet());/* 投标记录id */

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("order", borrowRepayment.getOrder())
                .eq("transferFlag", 0)
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);/* 查询未转让的投标记录回款记录 */
        Preconditions.checkNotNull(borrowCollectionList, "立即还款: 回款记录为空!");

        Map<Long /** tenderId */, BorrowCollection /** tender 对应的待收*/> borrowCollectionMap = borrowCollectionList
                .stream()
                .collect(Collectors.toMap(BorrowCollection::getTenderId, Function.identity()));

        Specification<UserThirdAccount> uta = Specifications
                .<UserThirdAccount>and()
                .in("userId", userIds.toArray())
                .build();
        List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(uta);/* 投资人存管账号信息 */
        Preconditions.checkNotNull(userThirdAccountList, "立即还款: 用户存管信息列表为空!");
        Map<Long /** userId */, UserThirdAccount /** tender 对应的待收*/> userThirdAccountMap = userThirdAccountList
                .stream()
                .collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));

        for (Tender tender : tenderList) { //循环迭代投标记录
            UserThirdAccount userThirdAccount = userThirdAccountMap.get(tender.getUserId());/* 投标人存管账户记录 */
            Preconditions.checkNotNull(userThirdAccount, "立即还款: 用户存管信息为空!");
            BorrowCollection borrowCollection = borrowCollectionMap.get(tender.getId());   /* 获取当前借款的回款记录 */
            if (ObjectUtils.isEmpty(borrowCollection)) { //出现回款记录为空的状况一般是转让标
                continue;
            }

            if (tender.getTransferFlag() == 1) {//查询正在转让中的记录，存在则取消
                Specification<Transfer> transferSpecification = Specifications
                        .<Transfer>and()
                        .in("status", 0, 1)
                        .eq("tenderId", tender.getId())
                        .build();

                List<Transfer> transferList = transferService.findList(transferSpecification);
                if (!CollectionUtils.isEmpty(transferList)) {
                    VoEndTransfer voEndTransfer = new VoEndTransfer();
                    voEndTransfer.setTransferId(transferList.get(0).getId());
                    voEndTransfer.setUserId(transferList.get(0).getUserId());
                    //取消债权转让
                    transferBiz.endTransfer(voEndTransfer);
                }
                tender.setTransferFlag(0);//设置转让标识
            }


            long collectionInterest = NumberHelper.toLong(borrowCollection.getInterest() * interestPercent);/* 当期应计利息 */
            long collectionMoney = borrowCollection.getPrincipal() + collectionInterest;//当期应还金额

            BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setType(AssetChangeTypeEnum.receivedPayments.getLocalType());  // 投资人收到还款
            batchAssetChangeItem.setUserId(tender.getUserId());
            batchAssetChangeItem.setToUserId(parentBorrow.getUserId());
            batchAssetChangeItem.setMoney(collectionMoney);
            batchAssetChangeItem.setInterest(collectionInterest);
            batchAssetChangeItem.setRemark(String.format("收到客户对借款[%s]第%s期的还款", BorrowHelper.getBorrowLink(parentBorrow.getId(), parentBorrow.getName()), (borrowCollection.getOrder() + 1)));
            if (advance) {
                batchAssetChangeItem.setRemark(String.format("收到广富宝对借款[%s]第%s期的垫付还款", BorrowHelper.getBorrowLink(parentBorrow.getId(), parentBorrow.getName()), (borrowCollection.getOrder() + 1)));
            }
            if (interestPercent < 1) {
                batchAssetChangeItem.setRemark("（提前结清）");
            }
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSourceId(borrowRepayment.getId());
            batchAssetChangeItem.setSeqNo(seqNo);
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItemService.save(batchAssetChangeItem);

            //扣除投资人待收
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setType(AssetChangeTypeEnum.collectionSub.getLocalType());  // 扣除债权转让人手续费
            batchAssetChangeItem.setUserId(tender.getUserId());
            batchAssetChangeItem.setToUserId(parentBorrow.getUserId());
            batchAssetChangeItem.setMoney(borrowCollection.getCollectionMoney());
            batchAssetChangeItem.setRemark(String.format("收到客户对[%s]借款的还款,扣除待收", BorrowHelper.getBorrowLink(parentBorrow.getId(), parentBorrow.getName())));
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSourceId(borrowRepayment.getId());
            batchAssetChangeItem.setSeqNo(seqNo);
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItemService.save(batchAssetChangeItem);


            //利息管理费
            if ((parentBorrow.getType() == 0) || (parentBorrow.getType() == 4) || parentBorrow.getType() == 3) {
                /**
                 * '2480 : 好人好梦',1753 : 红运当头',1699 : tasklist',3966 : 苗苗606',1413 : ljc_201',1857 : fanjunle',183 : 54435410',2327 : 栗子',2432 : 高翠西'2470 : sadfsaag',2552 : sadfsaag1',2739 : sadfsaag3',3939 : TinsonCheung',893 : kobayashi',608 : 0211',1216 : zqc9988'
                 */
                Set<String> stockholder = new HashSet<>(Arrays.asList("2480", "1753", "1699", "3966", "1413", "1857", "183", "2327", "2432", "2470", "2552", "2739", "3939", "893", "608", "1216"));
                if (!stockholder.contains(tender.getUserId())) {
                    Long feeAccountId = assetChangeProvider.getFeeAccountId();  // 平台ID
                    batchAssetChangeItem = new BatchAssetChangeItem();
                    batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                    batchAssetChangeItem.setState(0);
                    batchAssetChangeItem.setType(AssetChangeTypeEnum.interestManagementFee.getLocalType());  // 扣除投资人利息管理费
                    batchAssetChangeItem.setUserId(tender.getUserId());
                    batchAssetChangeItem.setToUserId(feeAccountId);
                    batchAssetChangeItem.setMoney(collectionInterest);
                    batchAssetChangeItem.setRemark(String.format("收到借款标[%s]利息管理费", BorrowHelper.getBorrowLink(parentBorrow.getId(), parentBorrow.getName())));
                    batchAssetChangeItem.setCreatedAt(nowDate);
                    batchAssetChangeItem.setUpdatedAt(nowDate);
                    batchAssetChangeItem.setSourceId(borrowRepayment.getId());
                    batchAssetChangeItem.setSeqNo(seqNo);
                    batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                    batchAssetChangeItemService.save(batchAssetChangeItem);
                    // 收费账户添加利息管理费用
                    batchAssetChangeItem = new BatchAssetChangeItem();
                    batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                    batchAssetChangeItem.setState(0);
                    batchAssetChangeItem.setType(AssetChangeTypeEnum.platformInterestManagementFee.getLocalType());  // 收费账户添加利息管理费用
                    batchAssetChangeItem.setUserId(feeAccountId);
                    batchAssetChangeItem.setToUserId(tender.getUserId());
                    batchAssetChangeItem.setMoney(collectionInterest);
                    batchAssetChangeItem.setRemark(String.format("收到借款标[%s]利息管理费", BorrowHelper.getBorrowLink(parentBorrow.getId(), parentBorrow.getName())));
                    batchAssetChangeItem.setCreatedAt(nowDate);
                    batchAssetChangeItem.setUpdatedAt(nowDate);
                    batchAssetChangeItem.setSourceId(borrowRepayment.getId());
                    batchAssetChangeItem.setSeqNo(seqNo);
                    batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                    batchAssetChangeItemService.save(batchAssetChangeItem);
                }
            }

            //逾期收入
            if (lateInterest > 0) {
                /* 投资人收入逾期利息 */
                long tempLateInterest = NumberHelper.toLong(tender.getValidMoney().doubleValue() / parentBorrow.getMoney().doubleValue() * lateInterest);

                batchAssetChangeItem = new BatchAssetChangeItem();
                batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                batchAssetChangeItem.setState(0);
                batchAssetChangeItem.setType(AssetChangeTypeEnum.receivedPaymentsPenalty.getLocalType());  // 收费账户添加利息管理费用
                batchAssetChangeItem.setUserId(tender.getUserId());
                batchAssetChangeItem.setToUserId(parentBorrow.getUserId());
                batchAssetChangeItem.setMoney(tempLateInterest);
                batchAssetChangeItem.setRemark(String.format("收到借款标[%s]的逾期罚息", BorrowHelper.getBorrowLink(parentBorrow.getId(), parentBorrow.getName())));
                batchAssetChangeItem.setCreatedAt(nowDate);
                batchAssetChangeItem.setUpdatedAt(nowDate);
                batchAssetChangeItem.setSourceId(borrowRepayment.getId());
                batchAssetChangeItem.setSeqNo(seqNo);
                batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                batchAssetChangeItemService.save(batchAssetChangeItem);
            }
        }
    }

    /**
     * 生成还款人还款批次资金改变记录
     *
     * @param borrowRepayment
     * @param
     */
    public void addBatchAssetChangeByBorrower(long batchAssetChangeId, BorrowRepayment borrowRepayment,
                                              Borrow borrow, double interestPercent,
                                              boolean isUserOpen, long lateInterest) {
        Date nowDate = new Date();
        String seqNo = assetChangeProvider.getSeqNo();
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();

        // 借款人还款
        BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setType(AssetChangeTypeEnum.repayment.getLocalType());  // 还款
        batchAssetChangeItem.setUserId(borrow.getUserId());
        batchAssetChangeItem.setMoney(borrowRepayment.getPrincipal() + borrowRepayment.getInterest());
        batchAssetChangeItem.setRemark(String.format("对借款[%s]第%s期的还款",
                BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()),
                StringHelper.toString(borrowRepayment.getOrder() + 1)));
        if (interestPercent < 1) {
            batchAssetChangeItem.setRemark("（提前结清）");
        } else if (!isUserOpen) {
            batchAssetChangeItem.setRemark("（系统自动还款）");
        }
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItem.setSourceId(borrowRepayment.getId());
        batchAssetChangeItem.setSeqNo(seqNo);
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItemService.save(batchAssetChangeItem);

        // 扣除借款人待还
        batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setType(AssetChangeTypeEnum.paymentSub.getLocalType());  // 扣除待还
        batchAssetChangeItem.setUserId(borrow.getUserId());
        batchAssetChangeItem.setMoney(borrowRepayment.getRepayMoney());
        batchAssetChangeItem.setInterest(borrowRepayment.getInterest());
        batchAssetChangeItem.setRemark("还款成功扣除待还");
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItem.setSourceId(borrowRepayment.getId());
        batchAssetChangeItem.setSeqNo(seqNo);
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItemService.save(batchAssetChangeItem);

        if ((lateInterest > 0)) {
            //扣除借款人还款滞纳金
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setType(AssetChangeTypeEnum.repayMentPenaltyFee.getLocalType());  // 扣除借款人还款滞纳金
            batchAssetChangeItem.setUserId(borrow.getUserId());
            batchAssetChangeItem.setMoney(lateInterest);
            batchAssetChangeItem.setRemark(String.format("借款[%s]的逾期罚息", BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName())));
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSourceId(borrowRepayment.getId());
            batchAssetChangeItem.setSeqNo(seqNo);
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);

        }
    }

    /**
     * 生成担保人代偿批次资金改变记录
     */
    public void addBatchAssetChangeByGuarantor(long batchAssetChangeId, BorrowRepayment borrowRepayment, Borrow parentBorrow,
                                               long lateInterest, String seqNo, String groupSeqNo) {
        Date nowDate = new Date();
        AdvanceLog advanceLog = advanceLogService.findByRepaymentId(borrowRepayment.getId());/* 还款垫付记录 */
        Preconditions.checkNotNull(advanceLog, "垫付记录不存在!");

        // 借款人偿还担保人代偿款
        BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setType(AssetChangeTypeEnum.compensatoryReceivedPayments.getLocalType());  // 借款人偿还担保人代偿款
        batchAssetChangeItem.setUserId(advanceLog.getUserId());
        batchAssetChangeItem.setToUserId(parentBorrow.getUserId());
        batchAssetChangeItem.setMoney(borrowRepayment.getRepayMoney() + lateInterest);/* 还款金额加上逾期利息 */
        batchAssetChangeItem.setRemark(String.format("收到客户对借款[%s]第%s期垫付的还款",
                BorrowHelper.getBorrowLink(parentBorrow.getId(), parentBorrow.getName()),
                (borrowRepayment.getOrder() + 1)));
        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItem.setSourceId(borrowRepayment.getId());
        batchAssetChangeItem.setSeqNo(seqNo);
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItemService.save(batchAssetChangeItem);
    }

    /**
     * @param userId
     * @param repayUserThirdAccount
     * @param borrowRepayment
     * @param borrow
     * @param lateInterest
     * @return
     * @throws Exception
     */
    private ResponseEntity<VoBaseResp> repayGuarantor(Long userId,
                                                      UserThirdAccount repayUserThirdAccount, BorrowRepayment borrowRepayment,
                                                      Borrow borrow, long lateInterest,
                                                      String batchNo) throws Exception {
        Date nowDate = new Date();
        log.info("借款人还款垫付人开始");
        List<RepayBail> repayBails = borrowRepaymentThirdBiz.calculateRepayBailPlan(borrow, repayUserThirdAccount.getAccountId(), getLateDays(borrowRepayment), borrowRepayment.getOrder(), lateInterest);
        double txAmount = repayBails.stream().mapToDouble(r -> NumberHelper.toDouble(r.getTxAmount())).sum();
        //所有交易利息
        double intAmount = repayBails.stream().mapToDouble(r -> NumberHelper.toDouble(r.getIntAmount())).sum();
        //所有还款手续费
        double txFeeOut = repayBails.stream().mapToDouble(r -> NumberHelper.toDouble(r.getTxFeeOut())).sum();
        //冻结金额
        double freezeMoney = txAmount + intAmount + txFeeOut;

        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(repayUserThirdAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(freezeMoney, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
            throw new Exception("即信借款人还款垫付人冻结资金失败：" + balanceFreezeResp.getRetMsg());
        }

        //立即还款冻结
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.Frozen);
        entity.setUserId(borrow.getUserId());
        entity.setMoney(new Double(freezeMoney * 100).longValue());
        entity.setRemark("借款人还款垫付人冻结可用资金");
        capitalChangeHelper.capitalChange(entity);

        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("userId", userId);
        acqResMap.put("repaymentId", borrowRepayment.getId());
        acqResMap.put("interestPercent", 1d);
        acqResMap.put("isUserOpen", true);
        acqResMap.put("freezeOrderId", orderId);
        acqResMap.put("freezeMoney", freezeMoney);

        BatchRepayBailReq request = new BatchRepayBailReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setSubPacks(GSON.toJson(repayBails));
        request.setTxCounts(StringHelper.toString(repayBails.size()));
        request.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/repaybail/check");
        request.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/repaybail/run");
        request.setAcqRes(GSON.toJson(acqResMap));
        request.setChannel(ChannelContant.HTML);
        BatchRepayBailResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY_BAIL, request, BatchRepayBailResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次融资人还担保账户垫款失败!"));
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowRepayment.getId());
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_REPAY_BAIL);
        thirdBatchLog.setRemark("批次融资人还担保账户垫款");
        thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
        thirdBatchLogService.save(thirdBatchLog);

        return ResponseEntity.ok(VoBaseResp.ok("批次融资人还担保账户垫款成功!"));

    }

    /**
     * @// TODO: 2017/8/3 在存管逾期收入加入到当期回款利息中  类型也需要修改
     */
    /**
     * 正常还款流程
     *
     * @param userId
     * @param repayUserThirdAccount
     * @param borrowRepayment
     * @param borrow
     * @param lateInterest
     * @param batchNo
     * @return
     * @throws Exception
     */
    private ResponseEntity<VoBaseResp> normalRepay(long userId,
                                                   UserThirdAccount repayUserThirdAccount, BorrowRepayment borrowRepayment,
                                                   Borrow borrow, long lateInterest,
                                                   String batchNo) throws Exception {
        Date nowDate = new Date();
        log.info("批次还款: 进入正常还款流程");
        List<Repay> repays = borrowRepaymentThirdBiz.calculateRepayPlan(borrow, repayUserThirdAccount.getAccountId(), borrowRepayment.getOrder(), getLateDays(borrowRepayment), lateInterest);
        //所有交易金额 交易金额指的是txAmount字段
        double txAmount = repays.stream().mapToDouble(r -> NumberHelper.toDouble(r.getTxAmount())).sum();
        //所有交易利息
        double intAmount = repays.stream().mapToDouble(r -> NumberHelper.toDouble(r.getIntAmount())).sum();
        //所有还款手续费
        double txFeeOut = repays.stream().mapToDouble(r -> NumberHelper.toDouble(r.getTxFeeOut())).sum();

        double freezeMoney = txAmount + txFeeOut + intAmount;/* 冻结金额 */

        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(repayUserThirdAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(freezeMoney, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, balanceFreezeResp.getRetMsg()));
        }

        //立即还款冻结
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.Frozen);
        entity.setUserId(userId);
        entity.setMoney(new Double((freezeMoney) * 100).longValue());
        entity.setRemark("立即还款冻结可用资金");
        capitalChangeHelper.capitalChange(entity);

        //
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("userId", userId);
        acqResMap.put("repaymentId", borrowRepayment.getId());
        acqResMap.put("interestPercent", 1d);
        acqResMap.put("isUserOpen", true);
        acqResMap.put("freezeOrderId", orderId);
        acqResMap.put("freezeMoney", freezeMoney);

        //批次还款操作
        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/repayDeal/run");
        request.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/repayDeal/check");
        request.setAcqRes(GSON.toJson(acqResMap));
        request.setSubPacks(GSON.toJson(repays));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(repays.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, response.getRetMsg()));
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowRepayment.getId());
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_REPAY);
        thirdBatchLog.setRemark("即信批次还款");
        thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
        thirdBatchLogService.save(thirdBatchLog);
        return ResponseEntity.ok(VoBaseResp.ok("还款正常"));
    }


    /**
     * 获取用户逾期费用
     * 逾期规则: 未还款本金之和 * 0.4$ 的费用, 平台收取 0.2%, 出借人 0.2%
     *
     * @param borrowRepayment
     * @param repaymentBorrow
     * @return
     */
    private int calculateLateInterest(int lateDays, BorrowRepayment borrowRepayment, Borrow repaymentBorrow) {

        if (0 == lateDays) {
            return 0;
        }

        long overPrincipal = borrowRepayment.getPrincipal();
        if (borrowRepayment.getOrder() < (repaymentBorrow.getTotalOrder() - 1)) { //
            Specification<BorrowRepayment> brs = Specifications
                    .<BorrowRepayment>and()
                    .eq("borrowId", repaymentBorrow.getId())
                    .eq("status", 0)
                    .build();
            List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
            Preconditions.checkNotNull(borrowRepayment, "批量放款: 计算逾期费用时还款计划为空");
            //剩余未还本金
            overPrincipal = borrowRepaymentList.stream().mapToLong(w -> w.getPrincipal()).sum();
        }

        return new Double(MathHelper.myRound(overPrincipal * 0.004 * lateDays, 2)).intValue();
    }

    private int getLateDays(BorrowRepayment borrowRepayment) {
        Date nowDateOfBegin = DateHelper.beginOfDate(new Date());
        Date repayDateOfBegin = DateHelper.beginOfDate(borrowRepayment.getRepayAt());
        int lateDays = DateHelper.diffInDays(nowDateOfBegin, repayDateOfBegin, false);
        lateDays = lateDays < 0 ? 0 : lateDays;
        return lateDays;
    }

    /**
     * 根据
     *
     * @param tranferedTender
     * @return
     */
    private Map<Long, Borrow> findTranferedBorrowByTender(List<Tender> tranferedTender) {
        Map<Long, Borrow> refMap = new HashMap<>();
        tranferedTender.forEach((Tender tender) -> {
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .eq("tenderId", tender.getId())
                    .eq("status", 3)
                    .build();
            List<Borrow> borrowList = borrowService.findList(bs);
            Preconditions.checkNotNull(borrowList, "批量还款: 查询转让标的为空");
            Borrow borrow = borrowList.get(0);
            refMap.put(tender.getId(), borrow);
        });

        return refMap;
    }


    /**
     * 查询已经债权转让成功投资记录
     *
     * @param tranferedTender
     * @return
     */
    private Map<Long, List<Tender>> findTranferedTenderRecord(List<Tender> tranferedTender) {

        Map<Long, List<Tender>> refMap = new HashMap<>();
        tranferedTender.forEach((Tender tender) -> {
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .eq("tenderId", tender.getId())
                    .eq("status", 3)
                    .build();
            List<Borrow> borrowList = borrowService.findList(bs);
            Preconditions.checkNotNull(borrowList, "批量还款: 查询转让标的为空");
            Borrow borrow = borrowList.get(0);

            Specification<Tender> specification = Specifications
                    .<Tender>and()
                    .eq("status", 1)
                    .eq("borrowId", borrow.getId())
                    .build();

            List<Tender> tranferedTenderList = tenderService.findList(specification);
            Preconditions.checkNotNull(tranferedTenderList, "批量还款: 获取投资记录列表为空");
            refMap.put(tender.getId(), tranferedTenderList);
        });
        return refMap;
    }

    /**
     * 查询投标记录中是否存在债权转让进行中的记录, 如果存在则进行取消债权转让
     *
     * @param tenderList
     */
    private void findTranferAndCancelTranfer(List<Tender> tenderList) throws Exception {
        // 债转进行中的记录
        List<Tender> tranferingTender = tenderList
                .stream()
                .filter(p -> p.getTransferFlag() == 1)
                .collect(Collectors.toList());
        for (Tender tender : tranferingTender) {
            doCancelTranfer(tender);
        }
    }

    /**
     * 取消债权转让
     *
     * @param tender
     * @throws Exception
     */
    private void doCancelTranfer(Tender tender) throws Exception {

    }

    /**
     * 查询还款计划
     *
     * @param order
     * @param tenderList
     * @return
     */
    private List<BorrowCollection> queryBorrowCollectionByTender(int order, List<Tender> tenderList) {
        Set<Long> tenderIdSet = tenderList.stream().map(p -> p.getId()).collect(Collectors.toSet());
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIdSet.toArray())
                .eq("status", 0)
                .eq("order", order)
                .build();

        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Preconditions.checkNotNull(borrowCollectionList, "批量还款: 查询还款计划为空");
        return borrowCollectionList;
    }

    /**
     * 获取正常投标记录
     *
     * @param borrowRepayment
     * @return
     */
    private List<Tender> queryTenderByRepayment(BorrowRepayment borrowRepayment) {
        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowRepayment.getBorrowId())
                .build();

        List<Tender> tenderList = tenderService.findList(specification);
        Preconditions.checkNotNull(tenderList, "批量还款: 获取投资记录列表为空");
        return tenderList;
    }


    /**
     * 用户还款前期判断
     * 1. 还款用户是否与还款计划用户一致
     * 2. 是否重复提交
     * 3. 判断是否跳跃还款
     *
     * @param userThirdAccount 用户开户
     * @param borrowRepayment  还款计划
     * @return
     */
    private ResponseEntity<VoBaseResp> repayConditionCheck(UserThirdAccount userThirdAccount, BorrowRepayment borrowRepayment) {
        // 1. 还款用户是否与还款计划用户一致
        if (!userThirdAccount.getUserId().equals(borrowRepayment.getUserId())) {
            log.error("批量还款: 还款前期判断, 还款计划用户与主动请求还款用户不匹配");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "非法操作: 还款计划与当前请求用户不一致!"));
        }

        // 2判断提交还款批次是否多次重复提交
        int flag = thirdBatchLogBiz.checkBatchOftenSubmit(String.valueOf(borrowRepayment.getId()),
                ThirdBatchLogContants.BATCH_REPAY_BAIL,
                ThirdBatchLogContants.BATCH_REPAY);
        if (flag > 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("还款处理中，请勿重复点击!")));
        }

        //  3. 判断是否跳跃还款
        Specification<BorrowRepayment> borrowRepaymentSpe = Specifications
                .<BorrowRepayment>and()
                .eq("id", borrowRepayment.getId())
                .eq("status", 0)
                .predicate(new LtSpecification<BorrowRepayment>("order", new DataObject(borrowRepayment.getOrder())))
                .build();
        List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(borrowRepaymentSpe);
        if (!CollectionUtils.isEmpty(borrowRepaymentList)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("该借款上一期还未还!")));
        }

        return ResponseEntity.ok(VoBaseResp.ok("验证成功"));

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
    private void receivedRepayBail(List<RepayBail> repayBails, Borrow borrow, String borrowUserThirdAccount, int order, double interestPercent, long lateInterest) throws Exception {
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

            List<Long> userIds = tenderList.stream().map(tender -> tender.getUserId()).collect(Collectors.toList());
            List<Long> tenderIds = tenderList.stream().map(tender -> tender.getId()).collect(Collectors.toList());

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
            long txAmount = 0;//融资人实际付出金额=交易金额+交易利息+还款手续费
            int intAmount = 0;//交易利息
            long principal = 0;
            int txFeeOut = 0;
            for (Tender tender : tenderList) {
                repayBail = new RepayBail();
                txAmount = 0;
                intAmount = 0;
                txFeeOut = 0;

                //当前借款的回款记录
                BorrowCollection borrowCollection = borrowCollectionList.stream()
                        .filter(bc -> StringHelper.toString(bc.getTenderId()).equals(StringHelper.toString(tender.getId())))
                        .collect(Collectors.toList()).get(0);

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
                    long tempLateInterest = tender.getValidMoney() / borrow.getMoney() * lateInterest;

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

                txAmount = principal;

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
        return newRepay(voRepayReq);
    }

    /**
     * 垫付检查
     *
     * @param repaymentId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> advanceCheck(Long repaymentId) throws Exception {
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

        //判断提交还款批次是否多次重复提交
        int flag = thirdBatchLogBiz.checkBatchOftenSubmit(String.valueOf(repaymentId), ThirdBatchLogContants.BATCH_BAIL_REPAY);
        if (flag == ThirdBatchLogContants.AWAIT) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("垫付处理中，请勿重复点击!")));
        } else if (flag == ThirdBatchLogContants.SUCCESS) {
            /**
             * @// TODO: 2017/7/18 增加本地查询
             */
        }

        long lateInterest = 0;//逾期利息
        int lateDays = 0;//逾期天数
        int diffDay = DateHelper.diffInDays(DateHelper.beginOfDate(new Date()), DateHelper.beginOfDate(borrowRepayment.getRepayAt()), false);
        if (diffDay > 0) {
            lateDays = diffDay;
            long overPrincipal = borrowRepayment.getPrincipal();//剩余未还本金
            if (order < (borrow.getTotalOrder() - 1)) {
                brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrow.getId())
                        .eq("status", 0)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                //剩余未还本金
                overPrincipal = borrowRepaymentList.stream().mapToLong(w -> w.getPrincipal()).sum();
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
        UserThirdAccount bailUserThirdAccount = userThirdAccountService.findByAccountId(borrow.getBailAccountId());//担保人存管信息

        //垫付金额 = 垫付本金 + 垫付利息
        double txAmount = bailRepayList.stream().mapToDouble(w -> NumberHelper.toDouble(w.getTxAmount())).sum();

        //批次号
        String batchNo = jixinHelper.getBatchNo();

        //====================================================================
        //冻结担保人账户资金
        //====================================================================
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(bailUserThirdAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(txAmount, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
            throw new Exception("即信批次担保人垫付冻结资金失败：" + balanceFreezeResp.getRetMsg());
        }

        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("repaymentId", repaymentId);
        acqResMap.put("freezeOrderId", orderId);
        acqResMap.put("accountId", bailUserThirdAccount.getAccountId());

        //立即还款冻结
        long frozenMoney = new Double(txAmount * 100).longValue();
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.Frozen);
        entity.setUserId(bailUserThirdAccount.getUserId());
        entity.setMoney(frozenMoney);
        entity.setRemark("担保人垫付冻结可用资金");
        capitalChangeHelper.capitalChange(entity);

        BatchBailRepayReq request = new BatchBailRepayReq();
        request.setChannel(ChannelContant.HTML);
        request.setBatchNo(batchNo);
        request.setAccountId(bailUserThirdAccount.getAccountId());
        request.setProductId(borrow.getProductId());
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setTxCounts(StringHelper.toString(bailRepayList.size()));
        request.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/bailrepay/check");
        request.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/bailrepay/run");
        request.setAcqRes(GSON.toJson(acqResMap));
        request.setSubPacks(GSON.toJson(bailRepayList));
        BatchBailRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_BAIL_REPAY, request, BatchBailRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次担保账户代偿失败!"));
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(repaymentId);
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_BAIL_REPAY);
        thirdBatchLog.setRemark("批次担保账户垫付");
        thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
        thirdBatchLogService.save(thirdBatchLog);

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
            long overPrincipal = borrowRepayment.getPrincipal();//剩余未还本金
            if (order < (borrow.getTotalOrder() - 1)) {
                brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrow.getId())
                        .eq("status", 0)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                //剩余未还本金
                overPrincipal = borrowRepaymentList.stream().mapToLong(w -> w.getPrincipal()).sum();
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
        advanceLog.setAdvanceMoneyYes((repayMoney + lateInterest));
        advanceLogService.insert(advanceLog);

        borrowRepayment.setLateDays(lateDays);
        borrowRepayment.setLateInterest(lateInterest);
        borrowRepayment.setAdvanceAtYes(new Date());
        borrowRepayment.setAdvanceMoneyYes((repayMoney + lateInterest));
        borrowRepaymentService.updateById(borrowRepayment);

        return ResponseEntity.ok(VoBaseResp.ok("垫付成功!"));
    }

    /**
     * 批次融资人还担保账户垫款
     *
     * @param voRepayReq
     */
    public ResponseEntity<VoBaseResp> thirdBatchRepayBail(VoRepayReq voRepayReq) throws Exception {
        Date nowDate = new Date();
        int lateInterest = 0;//逾期利息
        Double interestPercent = voRepayReq.getInterestPercent();
        Long repaymentId = voRepayReq.getRepaymentId();
        interestPercent = ObjectUtils.isEmpty(interestPercent) ? 1 : interestPercent;

        BorrowRepayment borrowRepayment = borrowRepaymentService.findByIdLock(repaymentId);
        Borrow borrow = borrowService.findById(borrowRepayment.getBorrowId());
        Long borrowId = borrow.getId();//借款ID

        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());

        // 逾期天数
        int lateDays = getLateDays(borrowRepayment);
        if (0 < lateDays) {  // 产生逾期
            long overPrincipal = borrowRepayment.getPrincipal();
            if (borrowRepayment.getOrder() < (borrow.getTotalOrder() - 1)) {
                Specification<BorrowRepayment> brs = Specifications.<BorrowRepayment>and()
                        .eq("status", 0)
                        .eq("borrowId", borrowId)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                Preconditions.checkNotNull(borrowRepayment, "还款信息不存在");
                //剩余未还本金
                overPrincipal = borrowRepaymentList.stream().mapToLong(br -> br.getPrincipal()).sum();
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

        double txAmount = repayBails.stream().mapToDouble(rb -> NumberHelper.toDouble(rb.getTxAmount())).sum();

        String batchNo = jixinHelper.getBatchNo();

        //====================================================================
        //冻结担保人账户资金
        //====================================================================
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(borrow.getBailAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(txAmount, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
            throw new Exception("即信批次还款冻结资金失败：" + balanceFreezeResp.getRetMsg());
        }


        BatchRepayBailReq request = new BatchRepayBailReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setSubPacks(GSON.toJson(repayBails));
        request.setTxCounts(StringHelper.toString(repayBails.size()));
        request.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/repaybail/check");
        request.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/repaybail/run");
        request.setAcqRes(GSON.toJson(voRepayReq));
        request.setChannel(ChannelContant.HTML);
        BatchRepayBailResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY_BAIL, request, BatchRepayBailResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "批次融资人还担保账户垫款失败!"));
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(repaymentId);
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_REPAY_BAIL);
        thirdBatchLog.setRemark("批次融资人还担保账户垫款");
        thirdBatchLog.setAcqRes(GSON.toJson(voRepayReq));
        thirdBatchLogService.save(thirdBatchLog);

        return ResponseEntity.ok(VoBaseResp.ok("批次融资人还担保账户垫款成功!"));
    }

}
