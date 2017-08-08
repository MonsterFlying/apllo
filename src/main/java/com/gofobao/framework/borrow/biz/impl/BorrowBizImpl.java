package com.gofobao.framework.borrow.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeReq;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayReq;
import com.gofobao.framework.api.model.batch_repay.BatchRepayResp;
import com.gofobao.framework.api.model.batch_repay.Repay;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.award.contants.RedPacketContants;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.borrow.vo.response.*;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.constans.JixinContants;
import com.gofobao.framework.common.constans.MoneyConstans;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.RandomHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.*;
import com.gofobao.framework.lend.entity.Lend;
import com.gofobao.framework.lend.service.LendService;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.entity.RepayAssetChange;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoBuildThirdRepayReq;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.system.biz.IncrStatisticBiz;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.*;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TenderThirdBiz;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.AutoTender;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.AutoTenderService;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateTenderReq;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by Zeke on 2017/5/26.
 */
@Service
@Slf4j
public class BorrowBizImpl implements BorrowBiz {

    static final Gson GSON = new Gson();

    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private AssetService assetService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private AutoTenderService autoTenderService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private MqHelper mqHelper;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private RepaymentBiz repaymentBiz;
    @Autowired
    private BorrowProvider borrowProvider;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;
    @Autowired
    private IncrStatisticBiz incrStatisticBiz;
    @Autowired
    private UserService userService;
    @Autowired
    private StatisticBiz statisticBiz;
    @Autowired
    private ThymeleafHelper thymeleafHelper;
    @Autowired
    private TenderThirdBiz tenderThirdBiz;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private LendService lendService;
    @Autowired
    private TenderBiz tenderBiz;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private BatchAssetChangeService batchAssetChangeService;
    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;
    @Autowired
    JixinManager jixinManager;
    @Autowired
    TransferBiz transferBiz;
    @Autowired
    AssetChangeProvider assetChangeProvider;
    @Autowired
    DictItemService dictItemService;
    @Autowired
    DictValueService dictValueService;
    @Autowired
    BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;
    @Autowired
    ThirdBatchLogService thirdBatchLogService;

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

    @Value("${gofobao.imageDomain}")
    private String imageDomain;

    /**
     * 提前结清
     *
     * @param voRepayAllReq
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> doRepayAll(VoRepayAllReq voRepayAllReq) throws Exception {
        String paramStr = voRepayAllReq.getParamStr();/* pc请求提前结清参数 */
        if (!SecurityHelper.checkSign(voRepayAllReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }
        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        /* 借款id */
        long borrowId = NumberHelper.toLong(paramMap.get("borrowId"));
        Borrow borrow = borrowService.findByIdLock(borrowId);/* 借款记录 */
        Preconditions.checkNotNull(borrow, "借款记录不存在!");
        UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());  /* 借款人存管账户不存在 */
        ResponseEntity<VoBaseResp> resp = ThirdAccountHelper.allConditionCheck(borrowUserThirdAccount);
        if (resp.getBody().getState().getCode() != VoBaseResp.OK) {
            return resp;
        }
        Asset borrowAsset = assetService.findByUserId(borrow.getUserId());/* 借款人资产账户 */
        Preconditions.checkNotNull(borrowAsset, "借款人资产记录不存在!");

        //判断提交还款批次是否多次重复提交
        int flag = thirdBatchLogBiz.checkBatchOftenSubmit(String.valueOf(borrowId), ThirdBatchLogContants.BATCH_REPAY);
        if (flag == ThirdBatchLogContants.AWAIT) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, StringHelper.toString("还款处理中，请勿重复点击!")));
        } else if (flag == ThirdBatchLogContants.SUCCESS) {
            //更新状态
            /**
             * @// TODO: 2017/8/7
             */
        }
        /* 有效未还的还款记录 */
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("borrowId", borrowId)
                .build();
        List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
        Map<Long/* repaymentId */, BorrowRepayment> borrowRepaymentMaps = borrowRepaymentList.stream().collect(Collectors.toMap(BorrowRepayment::getId, Function.identity()));
        /* 还款请求集合 */
        List<VoBuildThirdRepayReq> voBuildThirdRepayReqs = new ArrayList<>();
        //构建还款请求集合
        ImmutableSet<Long> resultSet = buildRepayReqList(voBuildThirdRepayReqs, borrow, borrowRepaymentList);
        Iterator<Long> iterator = resultSet.iterator();
        long penalty = iterator.next();/* 违约金 */
        long repayMoney = iterator.next();/* 提前结清需还总金额 */
        if (borrowAsset.getUseMoney() < (repayMoney)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "结清总共需要还款 " + repayMoney + " 元，您的账户余额不足，请先充值!！"));
        }
        /* 批次号 */
        String batchNo = jixinHelper.getBatchNo();
                /* 资产记录流水号 */
        String seqNo = assetChangeProvider.getSeqNo();
        /* 资产记录分组流水号 */
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        //生成批次资产改变主记录
        BatchAssetChange batchAssetChange = addBatchAssetChangeByRepayAll(batchNo, borrowId);
        //扣除提前结清的违约金
        addBatchAssetChangeByBorrowPenalty(borrowId, borrow, penalty, seqNo, groupSeqNo, batchAssetChange);
        //提前结清操作
        repayAll(borrowId, borrow, borrowUserThirdAccount, voBuildThirdRepayReqs, batchNo, seqNo, groupSeqNo, penalty, batchAssetChange, borrowRepaymentMaps);
        //提前结清操作
        return ResponseEntity.ok(VoBaseResp.ok("提前结清成功!"));
    }

    /**
     * 扣除提前结清的违约金
     *
     * @param borrowId
     * @param borrow
     * @param penalty
     * @param seqNo
     * @param groupSeqNo
     * @param batchAssetChange
     */
    private void addBatchAssetChangeByBorrowPenalty(long borrowId, Borrow borrow, long penalty, String seqNo, String groupSeqNo, BatchAssetChange batchAssetChange) {
        //扣除提前结清的违约金
        if (penalty > 0) {
            BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChange.getId());
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setType(AssetChangeTypeEnum.interestManagementFee.getLocalType());  // 扣除借款人违约金
            batchAssetChangeItem.setUserId(borrow.getUserId());
            batchAssetChangeItem.setMoney(penalty);
            batchAssetChangeItem.setRemark("扣除提前结清的违约金");
            batchAssetChangeItem.setCreatedAt(new Date());
            batchAssetChangeItem.setUpdatedAt(new Date());
            batchAssetChangeItem.setSourceId(borrowId);
            batchAssetChangeItem.setSeqNo(seqNo);
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItemService.save(batchAssetChangeItem);
        }
    }

    /**
     * 构建还款请求集合
     *
     * @param voBuildThirdRepayReqs 还款请求
     */
    private ImmutableSet<Long> buildRepayReqList(List<VoBuildThirdRepayReq> voBuildThirdRepayReqs, Borrow borrow, List<BorrowRepayment> borrowRepaymentList) {
        long repaymentTotal = 0;/* 还款总金额 */
        long penalty = 0;/* 违约金 */
        long repayMoney = 0;/* 还款总金额+违约金 */
        for (int i = 0; i < borrowRepaymentList.size(); i++) {
            BorrowRepayment borrowRepayment = borrowRepaymentList.get(i);
            if (borrowRepayment.getStatus() != 0) {
                continue;
            }
            /* 开始时间 */
            Date startAt;
            if (borrowRepayment.getOrder() == 0) {
                startAt = DateHelper.beginOfDate(borrow.getSuccessAt());
            } else {
                startAt = DateHelper.beginOfDate(borrowRepaymentList.get(i - 1).getRepayAt());
            }
            /* 结束时间 */
            Date endAt = DateHelper.beginOfDate(borrowRepayment.getRepayAt());

            //以结清第一期的14天利息作为违约金
            if (penalty == 0) { // 违约金
                penalty = borrowRepayment.getInterest() / DateHelper.diffInDays(endAt, startAt, false) * 14;
            }

            Date nowStartDate = DateHelper.beginOfDate(new Date());  // 现在的凌晨时间
            double interestPercent;/* 利息百分比 */
            if (nowStartDate.getTime() <= startAt.getTime()) {
                interestPercent = 0;
            } else {
                interestPercent = MathHelper.min(DateHelper.diffInDays(nowStartDate, startAt, false) / DateHelper.diffInDays(endAt, startAt, false), 1);
            }
            /* 逾期天数 */
            int lateDays = DateHelper.diffInDays(nowStartDate, endAt, false);
            /* 逾期利息 */
            long lateInterest = calculateLateInterest(lateDays, borrowRepayment, borrow);
            //累加金额用于判断还款账余额是否充足
            repaymentTotal += borrowRepayment.getPrincipal() + borrowRepayment.getInterest() * interestPercent + lateInterest;
            /* 还款请求 */
            VoBuildThirdRepayReq voBuildThirdRepayReq = new VoBuildThirdRepayReq();
            voBuildThirdRepayReq.setInterestPercent(interestPercent);   // 赔偿利息
            voBuildThirdRepayReq.setRepaymentId(borrowRepayment.getId());
            voBuildThirdRepayReq.setUserId(borrowRepayment.getUserId());
            voBuildThirdRepayReq.setIsUserOpen(false);
            voBuildThirdRepayReq.setLateDays(lateDays);
            voBuildThirdRepayReq.setLateInterest(lateInterest);
            voBuildThirdRepayReqs.add(voBuildThirdRepayReq);
        }
        repayMoney = repaymentTotal + penalty;/* 提前结清需还总金额 */
        return ImmutableSet.of(penalty, repayMoney);
    }

    /**
     * 提前结清操作
     *
     * @param borrowId
     * @param borrow
     * @param borrowUserThirdAccount
     * @param voBuildThirdRepayReqs
     * @param batchNo
     * @param seqNo
     * @param groupSeqNo
     * @param batchAssetChange
     * @param borrowRepaymentMaps
     * @throws Exception
     */
    private void repayAll(long borrowId, Borrow borrow, UserThirdAccount borrowUserThirdAccount, List<VoBuildThirdRepayReq> voBuildThirdRepayReqs, String batchNo,
                          String seqNo, String groupSeqNo, long penalty, BatchAssetChange batchAssetChange, Map<Long/* repaymentId */, BorrowRepayment> borrowRepaymentMaps) throws Exception {
        Date nowDate = new Date();
        List<Repay> repays = new ArrayList<>();/* 生成存管还款记录(提前结清) */
        for (VoBuildThirdRepayReq voBuildThirdRepayReq : voBuildThirdRepayReqs) {
            BorrowRepayment borrowRepayment = borrowRepaymentMaps.get(voBuildThirdRepayReq.getRepaymentId());/* 还款记录 */
            List<RepayAssetChange> repayAssetChangeList = new ArrayList<>();
            // 生成存管投资人还款记录(提前结清)
            List<Repay> tempRepays = borrowRepaymentThirdBiz.calculateRepayPlan(borrow, borrowUserThirdAccount.getAccountId(),
                    borrowRepayment.getOrder(), voBuildThirdRepayReq.getLateDays(), voBuildThirdRepayReq.getLateInterest(),
                    voBuildThirdRepayReq.getInterestPercent(), repayAssetChangeList);
            repays.addAll(tempRepays);
            // 生成还款记录
            repaymentBiz.doGenerateAssetChangeRecodeByRepay(borrow, borrowRepayment, borrowRepayment.getUserId(), repayAssetChangeList, batchAssetChange);
        }
        /* 总还款本金 */
        double sumTxAmount = repays.stream().mapToDouble(repay -> NumberHelper.toDouble(repay.getTxAmount())).sum();
        for (Repay repay : repays) {
            double partPenalty = NumberHelper.toDouble(repay.getTxAmount()) * sumTxAmount * penalty;/*分摊违约金*/
            //给每期回款分摊违约金
            repay.setTxFeeOut(StringHelper.formatDouble(NumberHelper.toDouble(repay.getTxFeeOut()) + partPenalty / 100.0, false));
        }

        //所有交易利息
        double intAmount = repays.stream().mapToDouble(r -> NumberHelper.toDouble(r.getIntAmount())).sum();
        //所有还款手续费
        double txFeeOut = repays.stream().mapToDouble(r -> NumberHelper.toDouble(r.getTxFeeOut())).sum();
        //冻结金额
        double freezeMoney = sumTxAmount + intAmount + txFeeOut;

        //====================================================================
        //冻结借款人账户资金
        //====================================================================
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);
        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(borrowUserThirdAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(freezeMoney, false));
        balanceFreezeReq.setOrderId(orderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
            throw new Exception("即信批次还款冻结资金失败：" + balanceFreezeResp.getRetMsg());
        }

        //请求保留参数
        Map<String, Object> acqResMap = new HashMap<>();
        acqResMap.put("borrowId", borrowId);
        acqResMap.put("freezeMoney", freezeMoney);
        acqResMap.put("freezeOrderId", orderId);
        acqResMap.put("userId", borrow.getUserId());

        //立即还款冻结
        long frozenMoney = new Double((freezeMoney) * 100).longValue();
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.Frozen);
        entity.setUserId(borrow.getUserId());
        entity.setMoney(frozenMoney);
        entity.setRemark("立即还款冻结可用资金");
        capitalChangeHelper.capitalChange(entity);

        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(sumTxAmount, false));
        request.setRetNotifyURL(javaDomain + "/pub/borrow/v2/third/repayall/run");
        request.setNotifyURL(javaDomain + "/pub/borrow/v2/third/repayall/check");
        request.setAcqRes(GSON.toJson(acqResMap));
        request.setSubPacks(GSON.toJson(repays));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(repays.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            throw new Exception("即信批次还款失败：" + response.getRetMsg());
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setSourceId(borrowId);
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_REPAY_ALL);
        thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
        thirdBatchLog.setRemark("(提前结清)即信批次还款");
        thirdBatchLogService.save(thirdBatchLog);
    }


    /**
     * 新增资产更改记录
     *
     * @param batchNo
     * @param borrowId
     * @return
     */
    private BatchAssetChange addBatchAssetChangeByRepayAll(String batchNo, long borrowId) {
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setSourceId(borrowId);
        batchAssetChange.setState(0);
        batchAssetChange.setType(BatchAssetChangeContants.BATCH_REPAY_ALL);/* 提前结清 */
        batchAssetChange.setCreatedAt(new Date());
        batchAssetChange.setUpdatedAt(new Date());
        batchAssetChange.setBatchNo(batchNo);
        batchAssetChangeService.save(batchAssetChange);
        return batchAssetChange;
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

    /**
     * 理财首页标列表
     *
     * @param voBorrowListReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewBorrowListWarpRes> findAll(VoBorrowListReq voBorrowListReq) {
        try {
            List<VoViewBorrowList> borrowLists = null;
            if (voBorrowListReq.getType() == 5) {  // 债权转让
                borrowLists = transferBiz.findTransferList(voBorrowListReq);
            } else {  // 正常标的
                borrowLists = borrowService.findNormalBorrow(voBorrowListReq);
            }

            VoViewBorrowListWarpRes listWarpRes = VoBaseResp.ok("查询成功", VoViewBorrowListWarpRes.class);
            listWarpRes.setVoViewBorrowLists(borrowLists);
            return ResponseEntity.ok(listWarpRes);
        } catch (Throwable e) {
            log.info("BorrowBizImpl findNormalBorrow fail%s", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewBorrowListWarpRes.class));
        }
    }


    @Override
    public ResponseEntity<VoPcBorrowList> pcFindAll(VoBorrowListReq voBorrowListReq) {

        try {
            VoPcBorrowList borrowLists = borrowService.pcFindAll(voBorrowListReq);
            VoPcBorrowList listWarpRes = VoBaseResp.ok("查询成功", VoPcBorrowList.class);
            listWarpRes.setBorrowLists(borrowLists.getBorrowLists());
            listWarpRes.setTotalCount(borrowLists.getTotalCount());
            listWarpRes.setPageIndex(borrowLists.getPageIndex());
            listWarpRes.setPageSize(borrowLists.getPageSize());
            return ResponseEntity.ok(listWarpRes);
        } catch (Throwable e) {
            log.info("BorrowBizImpl findNormalBorrow fail%s", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoPcBorrowList.class));
        }
    }


    /**
     * 标信息
     *
     * @param borrowId
     * @return
     */
    @Override
    public ResponseEntity<BorrowInfoRes> info(Long borrowId) {
        Borrow borrow = borrowService.findByBorrowId(borrowId);
        if (ObjectUtils.isEmpty(borrow)) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "非法查询",
                            BorrowInfoRes.class));
        }

        BorrowInfoRes borrowInfoRes = VoBaseResp.ok("查询成功", BorrowInfoRes.class);
        try {
            borrowInfoRes.setApr(StringHelper.formatMon(borrow.getApr() / 100d));
            borrowInfoRes.setLowest(StringHelper.formatMon(borrow.getLowest() / 100d));
            long surplusMoney = borrow.getMoney() - borrow.getMoneyYes();
            borrowInfoRes.setViewSurplusMoney(StringHelper.formatMon(surplusMoney / 100D));
            borrowInfoRes.setHideSurplusMoney(surplusMoney);
            if (borrow.getType() == BorrowContants.REPAY_FASHION_ONCE) {
                borrowInfoRes.setTimeLimit(borrow.getTimeLimit() + BorrowContants.DAY);
            } else {
                borrowInfoRes.setTimeLimit(borrow.getTimeLimit() + BorrowContants.MONTH);
            }
            double principal = 10000D * 100;
            double apr = NumberHelper.toDouble(StringHelper.toString(borrow.getApr()));
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(principal, apr, borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            Integer earnings = NumberHelper.toInt(calculatorMap.get("earnings"));
            borrowInfoRes.setEarnings(StringHelper.formatMon(earnings / 100d) + MoneyConstans.RMB);
            borrowInfoRes.setTenderCount(borrow.getTenderCount() + BorrowContants.TIME);
            borrowInfoRes.setMoney(StringHelper.formatMon(borrow.getMoney() / 100d));
            borrowInfoRes.setRepayFashion(borrow.getRepayFashion());
            borrowInfoRes.setSpend(Double.parseDouble(StringHelper.formatDouble(borrow.getMoneyYes() / borrow.getMoney().doubleValue(), false)));
            //结束时间
            Date endAt = DateHelper.addDays(DateHelper.beginOfDate(borrow.getReleaseAt()), borrow.getValidDay() + 1);
            borrowInfoRes.setEndAt(DateHelper.dateToString(endAt, DateHelper.DATE_FORMAT_YMDHMS));
            //进度
            borrowInfoRes.setSurplusSecond(-1L);
            //1.待发布 2.还款中 3.招标中 4.已完成 5.其它
            Integer status = borrow.getStatus();
            Date nowDate = new Date(System.currentTimeMillis());
            Date releaseAt = borrow.getReleaseAt();  //发布时间

            if (status == BorrowContants.BIDDING) {//招标中
                //待发布
                if (releaseAt.getTime() >= nowDate.getTime()) {
                    status = 1;
                    borrowInfoRes.setSurplusSecond(((releaseAt.getTime() - nowDate.getTime()) / 1000) + 5);
                } else if (nowDate.getTime() > endAt.getTime()) {  //当前时间大于招标有效时间
                    //流转标没有过期时间
                    if (!StringUtils.isEmpty(borrow.getTenderId())) {
                        status = 3; //招标中
                    } else {
                        status = 5; //已过期
                    }
                } else {
                    status = 3; //招标中
                    //  进度
                    double spend = Double.parseDouble(StringHelper.formatMon(borrow.getMoneyYes().doubleValue() / borrow.getMoney()));
                    if (spend == 1) {
                        status = 6;
                    }
                    borrowInfoRes.setSpend(spend);
                }
            } else if (!ObjectUtils.isEmpty(borrow.getSuccessAt()) && !ObjectUtils.isEmpty(borrow.getCloseAt())) {   //满标时间 结清
                status = 4; //已完成
                borrowInfoRes.setSpend(new Double(1));
            } else if (status == BorrowContants.PASS && ObjectUtils.isEmpty(borrow.getCloseAt())) {
                status = 2; //还款中
                borrowInfoRes.setSpend(new Double(1));
            }
            borrowInfoRes.setType(borrow.getType());
            if (!StringUtils.isEmpty(borrow.getTenderId())) {
                borrowInfoRes.setType(5);
            }

            borrowInfoRes.setPassWord(StringUtils.isEmpty(borrow.getPassword()) ? false : true);
            Users users = userService.findById(borrow.getUserId());
            borrowInfoRes.setUserName(!StringUtils.isEmpty(users.getUsername()) ? users.getUsername() : users.getPhone());
            borrowInfoRes.setIsNovice(borrow.getIsNovice());
            borrowInfoRes.setStatus(status);
            borrowInfoRes.setSuccessAt(StringUtils.isEmpty(borrow.getSuccessAt()) ? "" : DateHelper.dateToString(borrow.getSuccessAt()));
            borrowInfoRes.setBorrowName(borrow.getName());
            borrowInfoRes.setIsConversion(borrow.getIsConversion());
            borrowInfoRes.setIsNovice(borrow.getIsNovice());
            borrowInfoRes.setIsContinued(borrow.getIsContinued());
            borrowInfoRes.setIsImpawn(borrow.getIsImpawn());
            borrowInfoRes.setIsMortgage(borrow.getIsMortgage());
            borrowInfoRes.setIsVouch(borrow.getIsVouch());
            borrowInfoRes.setHideLowMoney(borrow.getLowest());
            borrowInfoRes.setIsFlow(StringUtils.isEmpty(borrow.getTenderId()) ? false : true);
            borrowInfoRes.setAvatar(imageDomain + "/data/images/avatar/" + borrow.getUserId() + "_avatar_small.jpg");
            borrowInfoRes.setReleaseAt(DateHelper.dateToString(borrow.getReleaseAt()));
            borrowInfoRes.setLockStatus(borrow.getIsLock());


            return ResponseEntity.ok(borrowInfoRes);
        } catch (Throwable e) {
            log.info("BorrowBizImpl detail fail%s", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            BorrowInfoRes.class));
        }

    }

    /**
     * 标简介
     *
     * @param borrowId
     * @return
     */
    @Override
    public ResponseEntity<VoViewVoBorrowDescWarpRes> desc(Long borrowId) {
        try {
            VoViewVoBorrowDescWarpRes borrowDescWarpRes = VoBaseResp.ok("查询成功", VoViewVoBorrowDescWarpRes.class);
            VoBorrowDescRes voBorrowDescRes = borrowService.desc(borrowId);
            borrowDescWarpRes.setVoBorrowDescRes(voBorrowDescRes);
            return ResponseEntity.ok(borrowDescWarpRes);
        } catch (Throwable e) {
            log.info("BorrowBizImpl desc fail%s", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewVoBorrowDescWarpRes.class));
        }
    }

    /**
     * pc:招标中统计
     *
     * @param
     * @return
     */
    @Override
    public ResponseEntity<VoViewBorrowStatisticsWarpRes> statistics() {
        try {
            VoViewBorrowStatisticsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewBorrowStatisticsWarpRes.class);
            List<BorrowStatistics> voBorrowDescRes = borrowService.statistics();
            warpRes.setStatisticsList(voBorrowDescRes);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            log.info("BorrowBizImpl desc fail%s", e);
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(
                            VoBaseResp.ERROR,
                            "查询失败",
                            VoViewBorrowStatisticsWarpRes.class));
        }
    }

    /**
     * 标合同
     *
     * @param borrowId
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> contract(Long borrowId, Long userId) {
        try {
            return borrowService.contract(borrowId, userId);
        } catch (Throwable e) {
            log.info("BorrowBizImpl contract error", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> pcContract(Long borrowId, Long userId) {
        try {
            return borrowService.pcContract(borrowId, userId);
        } catch (Throwable e) {
            log.info("BorrowBizImpl pcContract error", e);
            return null;
        }
    }

    /**
     * 新增借款
     *
     * @param voAddNetWorthBorrow
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> addNetWorth(VoAddNetWorthBorrow voAddNetWorthBorrow) throws Exception {
        Long userId = voAddNetWorthBorrow.getUserId();
        String releaseAtStr = voAddNetWorthBorrow.getReleaseAt();
        Integer money = (int) voAddNetWorthBorrow.getMoney();
        boolean closeAuto = voAddNetWorthBorrow.isCloseAuto();

        Asset asset = assetService.findByUserIdLock(userId);
        Preconditions.checkNotNull(asset, "净值标的发布: 当前用户资金账户为空!");
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        ResponseEntity<VoBaseResp> conditionCheckResponse = ThirdAccountHelper.allConditionCheck(userThirdAccount);
        if (!conditionCheckResponse.getStatusCode().equals(HttpStatus.OK)) {
            return conditionCheckResponse;
        }

        Date releaseAt = DateHelper.stringToDate(releaseAtStr, DateHelper.DATE_FORMAT_YMDHMS);
        if (releaseAt.getTime() > DateHelper.addDays(new Date(), 1).getTime()) {
            log.info("新增借款：发布时间必须在24小时内。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "发布时间必须在24小时内!"));
        }

        UserCache userCache = userCacheService.findById(userId);
        Preconditions.checkNotNull(userCache, "净值标的发布: 当前用户资金缓存账户为空!");

        double totalMoney = (asset.getUseMoney() + userCache.getWaitCollectionPrincipal()) * 0.8 - asset.getPayment();
        if (totalMoney < money) {
            log.info("新增借款：借款金额大于净值额度。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款金额大于净值额度!"));
        }

        long count = borrowService.countByUserIdAndStatusIn(userId, Arrays.asList(0, 1));
        if (count > 0) {
            log.info("新增借款：您已经有一个进行中的借款标。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "您已经有一个进行中的借款标!"));
        }

        Long borrowId = insertBorrow(voAddNetWorthBorrow, userId);  // 插入标
        if (borrowId <= 0) {
            log.info("新增借款：净值标插入失败。");
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "净值标插入失败!"));
        }

        if (closeAuto) { //关闭用户自动投标
            AutoTender saveAutoTender = new AutoTender();
            saveAutoTender.setStatus(false);
            saveAutoTender.setUpdatedAt(new Date());

            AutoTender condAutoTender = new AutoTender();
            condAutoTender.setUserId(userId);
            Example<AutoTender> autoTenderExample = Example.of(condAutoTender);

            if (!autoTenderService.updateByExample(saveAutoTender, autoTenderExample)) {
                log.info("新增借款：自动投标关闭失败。");
                return ResponseEntity
                        .badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "自动投标关闭失败!"));
            }
        }

        //初审
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
        mqConfig.setTag(MqTagEnum.FIRST_VERIFY);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        boolean mqState = false;
        try {
            log.info(String.format("borrowBizImpl firstVerify send mq %s", GSON.toJson(body)));
            mqState = mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("borrowBizImpl firstVerify send mq exception", e);
        }

        if (!mqState) {
            return ResponseEntity.ok(VoBaseResp.ok("发布净值借款失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("发布净值借款成功!"));
    }

    private long insertBorrow(VoAddNetWorthBorrow voAddNetWorthBorrow, Long userId) throws Exception {
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        Preconditions.checkNotNull(userThirdAccount, "借款人未开户!");

        Borrow borrow = new Borrow();
        borrow.setType(BorrowContants.JING_ZHI); // 净值标
        borrow.setUserId(userId);
        borrow.setTUserId(userThirdAccount.getId());
        borrow.setUse(0);
        borrow.setStatus(0);
        borrow.setIsLock(false);
        borrow.setIsImpawn(false);
        borrow.setIsContinued(false);
        borrow.setIsConversion(false);
        borrow.setIsNovice(false);
        borrow.setIsVouch(false);
        borrow.setIsMortgage(false);
        borrow.setName(voAddNetWorthBorrow.getName());
        borrow.setMoney(NumberHelper.toLong(voAddNetWorthBorrow.getMoney()));
        borrow.setRepayFashion(1);
        borrow.setTimeLimit(voAddNetWorthBorrow.getTimeLimit());
        borrow.setApr(voAddNetWorthBorrow.getApr());
        borrow.setLowest(50 * 100);
        borrow.setMost(0);
        borrow.setMostAuto(0);
        borrow.setValidDay(voAddNetWorthBorrow.getValidDay());
        borrow.setAward(0);
        borrow.setAwardType(0);
        String releaseAt = voAddNetWorthBorrow.getReleaseAt();
        if (!ObjectUtils.isEmpty(releaseAt)) {
            borrow.setReleaseAt(DateHelper.stringToDate(releaseAt, "yyyy-MM-dd HH:mm:ss"));
        }
        borrow.setDescription("");
        borrow.setPassword("");
        borrow.setMoneyYes(0l);
        borrow.setTenderCount(0);
        borrow.setCreatedAt(new Date());
        borrow.setUpdatedAt(new Date());
        boolean rs = borrowService.insert(borrow);
        if (rs) {
            return borrow.getId();
        } else {
            return 0;
        }
    }

    /**
     * 取消借款
     *
     * @param voCancelBorrow
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> cancelBorrow(VoCancelBorrow voCancelBorrow) throws Exception {
        Long borrowId = voCancelBorrow.getBorrowId();
        Date nowDate = new Date();
        Borrow borrow = borrowService.findByIdLock(borrowId);
        Preconditions.checkNotNull(borrow, "取消借款: 标的信息为空!");

        if ((borrow.getStatus() > 1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款状态已发生更改!"));
        }

        if (voCancelBorrow.getUserId() != borrow.getUserId()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "非法操作!"));
        }

        boolean bool = false;  // 债权转让默认不过期
        if (!ObjectUtils.isEmpty(borrow.getReleaseAt())) {
            Date limitDate = DateHelper.addDays(DateHelper.beginOfDate(borrow.getReleaseAt()), borrow.getValidDay() + 1);
            bool = limitDate.getTime() < nowDate.getTime();
        }

        if (((borrow.getStatus() == 1) && (bool))
                || (StringHelper.toString(borrow.getUserId()).equals(StringHelper.toString(voCancelBorrow.getUserId())))) {//只有借款标过期或者本人才能取消借款
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "只有借款标过期或者本人才能取消借款!"));
        }

        Specification<Tender> borrowSpecification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowId)
                .build();

        List<Tender> tenderList = tenderService.findList(borrowSpecification);
        Set<Long> userIdSet = tenderList.stream().map(p -> p.getUserId()).collect(Collectors.toSet());   // 投标的UserID

        // ======================================
        //  更改投资记录标识, 并且解冻投资资金
        // ======================================
        VoCancelThirdTenderReq voCancelThirdTenderReq = null;
        for (Tender tender : tenderList) {
            tender.setId(tender.getId());
            tender.setStatus(2); // 取消状态
            tender.setUpdatedAt(nowDate);
            tenderService.save(tender);

            //==================================================================
            //取消即信投资人投标记录
            //==================================================================
            if (!ObjectUtils.isEmpty(tender.getThirdTenderOrderId())) {
                voCancelThirdTenderReq = new VoCancelThirdTenderReq();
                voCancelThirdTenderReq.setTenderId(tender.getId());
                ResponseEntity<VoBaseResp> resp = tenderThirdBiz.cancelThirdTender(voCancelThirdTenderReq);
                log.info(String.format("取消借款: 发起即信存管投资取消!"));
                if (!resp.getStatusCode().equals(HttpStatus.OK)) {
                    log.error(String.format("取消借款:取消即信投标记录失败: %s", new Gson().toJson(resp)));
                    throw new Exception("borrowBizImpl cancelBorrow:" + resp.getBody().getState().getMsg());
                }
            }

            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Unfrozen);
            entity.setUserId(tender.getUserId());
            entity.setMoney(tender.getValidMoney());
            entity.setRemark("借款 [" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "] 招标失败解除冻结资金。");
            capitalChangeHelper.capitalChange(entity);
        }

        //==================================================================
        //即信取消标的
        //==================================================================
        cancelThirdBorrow(borrow);

        // ======================================
        //  发送站内信
        // ======================================
        Notices notices;
        String content = String.format("你所投资的借款[ %s ]在 %s 已取消", BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()), DateHelper.nextDate(nowDate));
        for (Long toUserId : userIdSet) {
            notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(toUserId);
            notices.setRead(false);
            notices.setName("投资的借款失败");
            notices.setContent(content);
            notices.setType("system");
            notices.setCreatedAt(nowDate);
            notices.setUpdatedAt(nowDate);
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            try {
                log.info(String.format("borrowBizImpl cancelBorrow send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("borrowBizImpl cancelBorrow send mq exception", e);
            }
        }

        // 债权转让标识取消
        assertAndDoTransfer(borrow);

        //更新借款
        borrow.setStatus(5);
        borrow.setUpdatedAt(nowDate);
        borrowService.updateById(borrow);
        return ResponseEntity.ok(VoBaseResp.ok("取消借款成功!"));
    }

    /**
     * @param borrows
     */
    @Transactional
    @Override
    public void schedulerCancelBorrow(List<Borrow> borrows) {
        Date nowDate = new Date();
        borrows.stream().filter(p -> !ObjectUtils.isEmpty(p) &&   //标不能为空
                p.getStatus() == 1 && //状态为招标中
                StringUtils.isEmpty(p.getTenderId()) &&  //不为转让标
                //招标已过有效期
                (DateHelper.addDays(DateHelper.beginOfDate(p.getReleaseAt()), p.getValidDay() + 1).getTime()) < nowDate.getTime());

        List<Long> borrowIds = borrows.stream().map(m -> m.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(borrowIds)) {
            log.info("调度取消过期标的失败----->查询标的为空");
            return;
        }
        borrows.stream().forEach(p->p.setStatus(BorrowContants.CANCEL));

        borrowService.save(borrows);

        Specification borrowSpecification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .in("borrowId", borrowIds.toArray())
                .build();
        List<Tender> tenderList = tenderService.findList(borrowSpecification);
        if (CollectionUtils.isEmpty(tenderList)) {
            log.info("调度取消过期标的失败----->投标记录为空");
            return;
        }
        //将所有的投标记录根据标的分组
        Map<Long, List<Tender>> tenderMaps = tenderList.stream().collect(groupingBy(Tender::getBorrowId));
        //所有标的map
        Map<Long, Borrow> borrowMap = borrows.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        // 投标的UserID
        Set<Long> userIds = tenderList.stream().map(p -> p.getUserId()).collect(Collectors.toSet());

        // ======================================
        //  更改投资记录标识, 并且解冻投资资金
        // ======================================
        VoCancelThirdTenderReq voCancelThirdTenderReq = null;

        for (Long key : tenderMaps.keySet()) {
            List<Tender> tenders = tenderMaps.get(key);
            for (Tender tender : tenders) {
                tender.setId(tender.getId());
                tender.setStatus(2); // 取消状态
                tender.setUpdatedAt(nowDate);
                tenderService.save(tender);
                //取消标的
                Borrow borrow = borrowMap.get(tender.getBorrowId());
                borrow.setStatus(BorrowContants.CANCEL);
                borrowService.save(borrow);
                //==================================================================
                //取消即信投资人投标记录
                //==================================================================
                if (!ObjectUtils.isEmpty(tender.getThirdTenderOrderId())) {
                    voCancelThirdTenderReq = new VoCancelThirdTenderReq();
                    voCancelThirdTenderReq.setTenderId(tender.getId());
                    ResponseEntity<VoBaseResp> resp = tenderThirdBiz.cancelThirdTender(voCancelThirdTenderReq);
                    log.info(String.format("取消借款: 发起即信存管投资取消!"));
                    if (!resp.getStatusCode().equals(HttpStatus.OK)) {
                        log.error(String.format("取消借款:取消即信投标记录失败: %s", new Gson().toJson(resp)));
                        log.info("borrowBizImpl cancelBorrow:" + resp.getBody().getState().getMsg());
                    }
                }

                // 冻结还款金额
                long money = new Double((tender.getValidMoney()) * 100).longValue();
                AssetChange freezeAssetChange = new AssetChange();
                freezeAssetChange.setForUserId(tender.getUserId());
                freezeAssetChange.setUserId(tender.getUserId());
                freezeAssetChange.setType(AssetChangeTypeEnum.unfreeze);
                freezeAssetChange.setRemark(String.format("标的取消成功[%s]解冻资金%s元", borrow.getName(), StringHelper.formatDouble(money / 100D, true)));
                freezeAssetChange.setSeqNo(assetChangeProvider.getSeqNo());
                freezeAssetChange.setMoney(money);
                freezeAssetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
                try {
                    assetChangeProvider.commonAssetChange(freezeAssetChange);
                } catch (Exception e) {
                    log.error(String.format("取消借款:资金变动失败"));
                }
            }
        }
        borrows.forEach(borrow -> {
            //==================================================================
            //即信取消标的
            //==================================================================
            try {
                cancelThirdBorrow(borrow);
            } catch (Exception e) {

            }
            // ======================================
            //  发送站内信
            // ======================================
            Notices notices;
            String content = String.format("你所投资的借款[ %s ]在 %s 已取消", BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()), DateHelper.nextDate(nowDate));
            for (Long toUserId : userIds) {
                notices = new Notices();
                notices.setFromUserId(1L);
                notices.setUserId(toUserId);
                notices.setRead(false);
                notices.setName("投资的借款失败");
                notices.setContent(content);
                notices.setType("system");
                notices.setCreatedAt(nowDate);
                notices.setUpdatedAt(nowDate);
                MqConfig mqConfig = new MqConfig();
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
                mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
                Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
                mqConfig.setMsg(body);
                try {
                    log.info(String.format("borrowBizImpl cancelBorrow send mq %s", GSON.toJson(body)));
                    mqHelper.convertAndSend(mqConfig);
                } catch (Throwable e) {
                    log.error("borrowBizImpl cancelBorrow send mq exception", e);
                }
            }

            // 债权转让标识取消
            try {
                assertAndDoTransfer(borrow);
            } catch (Exception e) {
                log.info("调度取消过期标的---->债权转让标识取消----->失败%s");
                log.info("当前标的信息:" + GSON.toJson(borrow));
            }
            //更新借款
            borrow.setStatus(5);
            borrow.setUpdatedAt(nowDate);
            borrowService.updateById(borrow);

        });


    }

    /**
     * 自动甄别该标是否属于债权转让. 是:自动取消债权转让标识
     *
     * @param borrow
     * @throws Exception
     */
    public void assertAndDoTransfer(Borrow borrow) throws Exception {
        Long tenderId = borrow.getTenderId();
        if ((borrow.getType() == 0) && (!ObjectUtils.isEmpty(tenderId)) && (tenderId > 0)) {
            Tender tender = tenderService.findById(tenderId);
            tender.setTransferFlag(0);
            tender.setUpdatedAt(new Date());
            tenderService.updateById(tender);
        }
    }


    /**
     * pc取消借款
     *
     * @param voPcCancelThirdBorrow
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> pcCancelBorrow(VoPcCancelThirdBorrow voPcCancelThirdBorrow) throws Exception {
        Date nowDate = new Date();
        String paramStr = voPcCancelThirdBorrow.getParamStr();
        if (!SecurityHelper.checkSign(voPcCancelThirdBorrow.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc取消借款 签名验证不通过!"));
        }

        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long borrowId = NumberHelper.toLong(paramMap.get("borrowId"));

        Borrow borrow = borrowService.findByIdLock(borrowId);
        if (ObjectUtils.isEmpty(borrow)
                || (borrow.getStatus() != 0 && borrow.getStatus() != 1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款状态已发生更改!"));
        }

        if (borrow.getMoneyYes() / borrow.getMoney() == 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "满标后标的不可以撤销!"));
        }

        Specification<Tender> borrowSpecification = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", borrowId)
                .build();

        List<Tender> tenderList = tenderService.findList(borrowSpecification);
        Set<Long> userIdSet = tenderList.stream().map(p -> p.getUserId()).collect(Collectors.toSet());   // 投标的UserID

        // ======================================
        //  更改投资记录标识, 并且解冻投资资金
        // ======================================
        VoCancelThirdTenderReq voCancelThirdTenderReq = null;
        for (Tender tender : tenderList) {
            tender.setUpdatedAt(nowDate);
            tender.setStatus(2);
            tenderService.save(tender);

            //==================================================================
            //取消即信投资人投标记录
            //==================================================================
            if (!ObjectUtils.isEmpty(tender.getThirdTenderOrderId())) {
                voCancelThirdTenderReq = new VoCancelThirdTenderReq();
                voCancelThirdTenderReq.setTenderId(tender.getId());
                ResponseEntity<VoBaseResp> resp = tenderThirdBiz.cancelThirdTender(voCancelThirdTenderReq);
                if (!resp.getStatusCode().equals(HttpStatus.OK)) {
                    throw new Exception("borrowBizImpl pcCancelBorrow:" + resp.getBody().getState().getMsg());
                }
            }

            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setType(CapitalChangeEnum.Unfrozen);
            entity.setUserId(tender.getUserId());
            entity.setMoney(tender.getValidMoney());
            entity.setRemark("借款 [" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "] 招标失败解除冻结资金。");
            capitalChangeHelper.capitalChange(entity);
        }

        //==================================================================
        //即信取消标的
        //==================================================================
        cancelThirdBorrow(borrow);

        // ======================================
        //  发送站内信
        // ======================================
        Notices notices;
        String content = String.format("你所投资的借款[ %s ]在 %s 已取消", BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()), DateHelper.nextDate(nowDate));
        for (Long toUserId : userIdSet) {
            notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(toUserId);
            notices.setRead(false);
            notices.setName("投资的借款失败");
            notices.setContent(content);
            notices.setType("system");
            notices.setCreatedAt(nowDate);
            notices.setUpdatedAt(nowDate);
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_NOTICE);
            mqConfig.setTag(MqTagEnum.NOTICE_PUBLISH);
            Map<String, String> body = GSON.fromJson(GSON.toJson(notices), TypeTokenContants.MAP_TOKEN);
            mqConfig.setMsg(body);
            try {
                log.info(String.format("borrowBizImpl pcCancelBorrow send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
            } catch (Throwable e) {
                log.error("borrowBizImpl pcCancelBorrow send mq exception", e);
            }
        }

        // 债权转让标识取消
        assertAndDoTransfer(borrow);

        //更新借款
        borrow.setStatus(5);
        borrow.setUpdatedAt(nowDate);
        borrowService.updateById(borrow);
        return ResponseEntity.ok(VoBaseResp.ok("取消借款成功!"));
    }

    /**
     * 取消第三方标的
     *
     * @param borrow
     */
    private void cancelThirdBorrow(Borrow borrow) throws Exception {
        //================================即信取消标的==================================
        String productId = borrow.getProductId();
        if (!StringUtils.isEmpty(productId)) {

            Map<String, Object> map = jdbcTemplate.queryForMap("select count(id) as count from gfb_borrow_tender where borrow_id = " + borrow.getId() + " and third_tender_order_id is not null AND third_tender_cancel_order_id is NULL ");
            if (NumberHelper.toInt(map.get("count")) <= 0) {
                VoCancelThirdBorrow voCancelThirdBorrow = new VoCancelThirdBorrow();
                voCancelThirdBorrow.setProductId(productId);
                voCancelThirdBorrow.setUserId(borrow.getUserId());
                voCancelThirdBorrow.setRaiseDate(DateHelper.dateToString(borrow.getReleaseAt(), DateHelper.DATE_FORMAT_YMD_NUM));
                voCancelThirdBorrow.setAcqRes(StringHelper.toString(borrow.getId()));
                ResponseEntity<VoBaseResp> resp = borrowThirdBiz.cancelThirdBorrow(voCancelThirdBorrow);
                if (!resp.getStatusCode().equals(HttpStatus.OK)) {
                    throw new Exception("borrowBizImpl cancelThirdBorrow:" + resp.getBody().getState().getMsg());
                }
            } else {
                log.error("当前标定中还存在未取消投标申请记录");
                throw new Exception("borrowBizImpl cancelThirdBorrow: 当前标定中还存在未取消投标申请记录");
            }
        }
    }

    /**
     * 非转让标复审
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Throwable.class)
    public boolean borrowAgainVerify(Borrow borrow) throws Exception {
        if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 1)
                || (!StringHelper.toString(borrow.getMoney()).equals(StringHelper.toString(borrow.getMoneyYes())))) {
            return false;
        }
        Date nowDate = new Date();
        // 生成还款记录
        disposeBorrowRepay(borrow, nowDate);

        //查询当前借款的所有 状态为1的 tender记录
        Specification<Tender> ts = Specifications.<Tender>and()
                .eq("borrowId", borrow.getId())
                .eq("status", 1)
                .build();
        List<Tender> tenderList = tenderService.findList(ts);
        Preconditions.checkNotNull(tenderList, "生成还款记录: 投标记录为空");
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        // 这里涉及用户投标回款计划生成和平台资金的变动
        generateBorrowCollectionAndAssetChange(borrow, tenderList, nowDate, groupSeqNo);

        // 标的自身设置奖励信息:进行存管红包发放
        awardUserByBorrowTender(borrow, tenderList);

        // 发送投资成功站内信
        sendNoticsByTender(borrow, tenderList);

        // 用户投标信息和每日统计
        userTenderStatistic(borrow, tenderList, nowDate);

        //用戶投資送紅包
        userTenderRedPackage(borrow, tenderList, nowDate, groupSeqNo);

        // 借款人资金变动
        processBorrowAssetChange(borrow, tenderList, nowDate, groupSeqNo);

        // 满标操作
        finishBorrow(borrow);

        // 复审事件
        //如果是流转标则扣除 自身车贷标待收本金 和 推荐人的邀请用户车贷标总待收本金
        updateUserCacheByBorrowReview(borrow);

        //更新网站统计
        updateStatisticByBorrowReview(borrow);

        //借款成功发送通知短信
        smsNoticeByBorrowReview(borrow);

        //发送借款协议
        sendBorrowProtocol(borrow);
        return true;
    }

    /**
     * 生成还款记录
     *
     * @param borrow
     * @param nowDate
     */
    private void disposeBorrowRepay(Borrow borrow, Date nowDate) {
        // 调用利息计算器得出借款每期应还信息
        BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(NumberHelper.toDouble(StringHelper.toString(borrow.getMoney())),
                NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrow.getSuccessAt());
        Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
        List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");
        BorrowRepayment borrowRepayment = null;
        for (int i = 0; i < repayDetailList.size(); i++) {
            borrowRepayment = new BorrowRepayment();
            Map<String, Object> repayDetailMap = repayDetailList.get(i);
            borrowRepayment.setBorrowId(borrow.getId());
            borrowRepayment.setStatus(0);
            borrowRepayment.setOrder(i);
            borrowRepayment.setRepayAt(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
            borrowRepayment.setRepayMoney(NumberHelper.toLong(repayDetailMap.get("repayMoney")));
            borrowRepayment.setPrincipal(NumberHelper.toLong(repayDetailMap.get("principal")));
            borrowRepayment.setInterest(NumberHelper.toLong(repayDetailMap.get("interest")));
            borrowRepayment.setRepayMoneyYes(0l);
            borrowRepayment.setCreatedAt(nowDate);
            borrowRepayment.setUpdatedAt(nowDate);
            borrowRepayment.setAdvanceMoneyYes(0l);
            borrowRepayment.setLateDays(0);
            borrowRepayment.setLateInterest(0l);
            borrowRepayment.setUserId(borrow.getUserId());
            borrowRepaymentService.save(borrowRepayment);
        }
    }

    /**
     * 结束标的信息
     *
     * @param borrow
     */
    private void finishBorrow(Borrow borrow) {
        log.info(String.format("批处理: 更改标的为满标 %s", new Gson().toJson(borrow)));
        borrow.setStatus(3);
        borrow.setSuccessAt(new Date());
        borrowService.updateById(borrow);
    }

    /**
     * 处理借款人资金变动问题
     *
     * @param borrow
     * @param tenderList
     * @param startAt
     * @param groupSeqNo
     * @throws Exception
     */
    private void processBorrowAssetChange(Borrow borrow, List<Tender> tenderList, Date startAt, String groupSeqNo) throws Exception {

        // 放款
        AssetChange borrowAssetChangeEntity = new AssetChange();
        borrowAssetChangeEntity.setSourceId(borrow.getId());
        borrowAssetChangeEntity.setGroupSeqNo(groupSeqNo);
        borrowAssetChangeEntity.setMoney(borrow.getMoney());
        borrowAssetChangeEntity.setSeqNo(assetChangeProvider.getSeqNo());
        borrowAssetChangeEntity.setRemark(String.format("标的[%s]融资成功. 放款%s元", borrow.getName(), StringHelper.formatDouble(borrow.getMoney() / 100D, true)));
        borrowAssetChangeEntity.setType(AssetChangeTypeEnum.borrow);
        borrowAssetChangeEntity.setUserId(ObjectUtils.isEmpty(borrow.getTakeUserId()) ? borrow.getUserId() : borrow.getTakeUserId());
        assetChangeProvider.commonAssetChange(borrowAssetChangeEntity);  // 放款

        // 获取待还
        long feeId = assetChangeProvider.getFeeAccountId();  // 收费账户
        long collectionMoney = 0;
        long collectionInterest = 0;
        for (Tender tender : tenderList) {
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                    NumberHelper.toDouble(StringHelper.toString(tender.getValidMoney())),
                    NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), startAt);
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");
            Preconditions.checkNotNull(repayDetailList, "生成用户回款计划开始: 计划生成为空");
            for (int i = 0; i < repayDetailList.size(); i++) {
                Map<String, Object> repayDetailMap = repayDetailList.get(i);
                collectionMoney += NumberHelper.toLong(repayDetailMap.get("repayMoney"));
                collectionInterest += NumberHelper.toLong(repayDetailMap.get("interest"));
            }
        }

        // 添加待还
        AssetChange paymentAssetChangeEntity = new AssetChange();
        paymentAssetChangeEntity.setSourceId(borrow.getId());
        paymentAssetChangeEntity.setGroupSeqNo(groupSeqNo);
        paymentAssetChangeEntity.setMoney(collectionMoney);
        paymentAssetChangeEntity.setInterest(collectionInterest);
        paymentAssetChangeEntity.setSeqNo(assetChangeProvider.getSeqNo());
        paymentAssetChangeEntity.setRemark(String.format("添加待还金额%s元", StringHelper.formatDouble(collectionMoney / 100D, true)));
        paymentAssetChangeEntity.setType(AssetChangeTypeEnum.paymentAdd);
        paymentAssetChangeEntity.setUserId(borrow.getUserId());
        assetChangeProvider.commonAssetChange(paymentAssetChangeEntity);  // 放款

        // 净值账户管理费
        if (borrow.getType() == 1) {
            Double fee;
            if (borrow.getRepayFashion() == 1) {
                fee = MathHelper.myRound(borrow.getMoney() * 0.0012 / 30 * borrow.getTimeLimit(), 2);
            } else {
                fee = MathHelper.myRound(borrow.getMoney() * 0.0012 * borrow.getTimeLimit(), 2);
            }
            AssetChange outBorrowFeeAssetChangeEntity = new AssetChange();
            outBorrowFeeAssetChangeEntity.setSourceId(borrow.getId());
            outBorrowFeeAssetChangeEntity.setGroupSeqNo(groupSeqNo);
            outBorrowFeeAssetChangeEntity.setMoney(fee.longValue());
            outBorrowFeeAssetChangeEntity.setSeqNo(assetChangeProvider.getSeqNo());
            outBorrowFeeAssetChangeEntity.setRemark(String.format("扣除标的[%s]融资管理费%s元", borrow.getName(), StringHelper.formatDouble(fee / 100D, true)));
            outBorrowFeeAssetChangeEntity.setType(AssetChangeTypeEnum.financingManagementFee);
            outBorrowFeeAssetChangeEntity.setUserId(borrow.getUserId());
            outBorrowFeeAssetChangeEntity.setForUserId(feeId);
            assetChangeProvider.commonAssetChange(outBorrowFeeAssetChangeEntity);  // 扣除融资管理费

            // 费用平台添加收取的转让费
            AssetChange inBorrowFeeAssetChangeEntity = new AssetChange();
            inBorrowFeeAssetChangeEntity.setSourceId(borrow.getId());
            inBorrowFeeAssetChangeEntity.setGroupSeqNo(groupSeqNo);
            inBorrowFeeAssetChangeEntity.setMoney(fee.longValue());
            inBorrowFeeAssetChangeEntity.setSeqNo(assetChangeProvider.getSeqNo());
            inBorrowFeeAssetChangeEntity.setRemark(String.format("收取标的[%s]融资管理费%s元", borrow.getName(), StringHelper.formatDouble(fee / 100D, true)));
            inBorrowFeeAssetChangeEntity.setType(AssetChangeTypeEnum.platformFinancingManagementFee);
            inBorrowFeeAssetChangeEntity.setUserId(feeId);
            inBorrowFeeAssetChangeEntity.setForUserId(borrow.getUserId());
            assetChangeProvider.commonAssetChange(inBorrowFeeAssetChangeEntity);  // 收取融资管理费
        }
    }

    /**
     * 用户投标统计
     *
     * @param borrow
     * @param tenderList
     * @param startAt
     */
    private void userTenderStatistic(Borrow borrow, List<Tender> tenderList, Date startAt) throws Exception {
        Gson gson = new Gson();
        for (Tender tender : tenderList) {
            log.info(String.format("投标统计: %s", gson.toJson(tender)));
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                    NumberHelper.toDouble(StringHelper.toString(tender.getValidMoney())),
                    NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), startAt);
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");
            Preconditions.checkNotNull(repayDetailList, "生成用户回款计划开始: 计划生成为空");
            long countInterest = 0;
            for (int i = 0; i < repayDetailList.size(); i++) {
                Map<String, Object> repayDetailMap = repayDetailList.get(i);
                countInterest += NumberHelper.toLong(repayDetailMap.get("interest"));
            }

            UserCache userCache = userCacheService.findById(tender.getUserId());
            if (borrow.getType() == 0) {
                userCache.setTjWaitCollectionPrincipal(userCache.getTjWaitCollectionPrincipal() + tender.getValidMoney());
                userCache.setTjWaitCollectionInterest(userCache.getTjWaitCollectionInterest() + countInterest);
            }

            if (borrow.getType() == 4) {
                userCache.setQdWaitCollectionPrincipal(userCache.getQdWaitCollectionPrincipal() + tender.getValidMoney());
                userCache.setQdWaitCollectionInterest(userCache.getQdWaitCollectionInterest() + countInterest);
            }

            IncrStatistic incrStatistic = new IncrStatistic();
            if ((!userCache.getTenderTransfer()) && (!userCache.getTenderTuijian()) && (!userCache.getTenderJingzhi()) && (!userCache.getTenderMiao()) && (!userCache.getTenderQudao())) {
                incrStatistic.setTenderCount(1);
                incrStatistic.setTenderTotal(1);
            }

            if (borrow.isTransfer() && (!userCache.getTenderTransfer())) {
                incrStatistic.setTenderLzCount(1);
                incrStatistic.setTenderLzTotalCount(1);
                userCache.setTenderTransfer(true);
            } else if ((borrow.getType() == 0) && (!userCache.getTenderTuijian())) {
                incrStatistic.setTenderTjCount(1);
                incrStatistic.setTenderTjTotalCount(1);
                userCache.setTenderTuijian(true);
            } else if ((borrow.getType() == 1) && (!userCache.getTenderJingzhi())) {
                incrStatistic.setTenderJzCount(1);
                incrStatistic.setTenderJzTotalCount(1);
                userCache.setTenderJingzhi(true);
            } else if ((borrow.getType() == 2) && (!userCache.getTenderMiao())) {
                incrStatistic.setTenderMiaoCount(1);
                incrStatistic.setTenderMiaoTotalCount(1);
                userCache.setTenderMiao(true);
            } else if ((borrow.getType() == 4) && (!userCache.getTenderQudao())) {
                incrStatistic.setTenderQdCount(1);
                incrStatistic.setTenderQdTotalCount(1);
                userCache.setTenderQudao(true);
            }

            userCacheService.save(userCache);
            if (!ObjectUtils.isEmpty(incrStatistic)) {
                incrStatisticBiz.caculate(incrStatistic);
            }
        }
    }

    /**
     * 用戶投資送紅包處理
     *
     * @param borrow
     * @param tenderList
     * @param nowDate
     * @param groupSeqNo
     */
    public void userTenderRedPackage(Borrow borrow, List<Tender> tenderList, Date nowDate, String groupSeqNo) {
        log.info("红包-----即信放款处理成功,进入红包条件判断中-----------");
        Gson gson = new Gson();
        tenderList.forEach(p -> {
            UserCache userCache = userCacheService.findById(p.getUserId());
            Map<String, Object> paramsMap = new HashMap<>();
            //=================================
            // 非新手标  是新手标但是老用投
            //===================================
            boolean access = (!borrow.getIsNovice()) || (borrow.getIsNovice() && (userCache.getTenderTuijian() || userCache.getTenderQudao()));
            if (access) {
                try {
                    paramsMap.put("type", RedPacketContants.OLD_USER_TENDER_BORROW_REDPACKAGE);
                    paramsMap.put("tenderId", p.getId());
                    paramsMap.put("time", DateHelper.dateToString(nowDate));
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_RED_PACKAGE);
                    mqConfig.setTag(MqTagEnum.OLD_USER_TENDER);
                    Map<String, String> body = GSON.fromJson(GSON.toJson(paramsMap), TypeTokenContants.MAP_TOKEN);
                    mqConfig.setMsg(body);
                    mqHelper.convertAndSend(mqConfig);
                    log.info(String.format("触发老手投标红包MQ %s", gson.toJson(paramsMap)));
                } catch (Exception e) {
                    log.error(String.format("老用户投资红包推送异常：%s", gson.toJson(paramsMap)), e);
                }
            }

            //======================================
            // 推荐用户投资红包
            //======================================
            paramsMap.clear();
            paramsMap.put("type", RedPacketContants.INVITE_USER_TENDER_BORROW_REDPACKAGE);
            paramsMap.put("tenderId", p.getId());
            paramsMap.put("time", DateHelper.dateToString(nowDate));
            try {
                MqConfig mqConfig = new MqConfig();
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_RED_PACKAGE);
                mqConfig.setTag(MqTagEnum.INVITE_USER_TENDER);
                Map<String, String> body = GSON.fromJson(GSON.toJson(paramsMap), TypeTokenContants.MAP_TOKEN);
                mqConfig.setMsg(body);
                mqHelper.convertAndSend(mqConfig);
                log.info(String.format("触发邀请用户投资红包MQ%s", gson.toJson(paramsMap)));
            } catch (Exception e) {
                log.error(String.format("邀请用户投资红包推送异常：%s", gson.toJson(paramsMap)), e);
            }
            paramsMap.clear();
            boolean accessState = userCache.getTenderTuijian() || userCache.getTenderQudao();
            //=========================
            // 新手标，而且未投标过
            //======================
            if ((borrow.getIsNovice()) && (!accessState)) {
                paramsMap.put("type", RedPacketContants.NEW_USER_BORROW_REDPACKAGE);
                paramsMap.put("tenderId", p.getId());
                String tranId = RandomHelper.generateNumberCode(4) + System.currentTimeMillis();
                paramsMap.put("transactionId", tranId);
                paramsMap.put("time", DateHelper.dateToString(nowDate));
                try {
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_RED_PACKAGE);
                    mqConfig.setTag(MqTagEnum.NEW_USER_TENDER);
                    Map<String, String> body = GSON.fromJson(GSON.toJson(paramsMap), TypeTokenContants.MAP_TOKEN);
                    mqConfig.setMsg(body);
                    mqHelper.convertAndSend(mqConfig);
                    log.info(String.format("触发新手投标红包MQ %s", gson.toJson(paramsMap)));
                } catch (Exception e) {
                    log.error(String.format("新手投标红包推送异常：%s", gson.toJson(paramsMap)), e);
                }
            }
        });

    }


    /**
     * 发送投资成功站内信
     *
     * @param borrow
     * @param tenderList
     */
    private void sendNoticsByTender(Borrow borrow, List<Tender> tenderList) {
        Gson gson = new Gson();
        log.info(String.format("发送投标成功站内信开始: %s", gson.toJson(tenderList)));
        Date nowDate = new Date();
        Set<Long> userIdSet = tenderList.stream().map(tender -> tender.getUserId()).collect(Collectors.toSet());
        for (Long userId : userIdSet) {
            Notices notices = new Notices();
            notices.setFromUserId(1L);
            notices.setUserId(userId);
            notices.setRead(false);
            notices.setName("投资的借款满标审核通过");
            notices.setContent("您所投资的借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]在 " + DateHelper.dateToString(nowDate) + " 已满标审核通过");
            notices.setType("system");
            notices.setCreatedAt(nowDate);
            notices.setUpdatedAt(nowDate);
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
        log.info(String.format("发送投标成功站内信结束:  %s", gson.toJson(tenderList)));
    }

    /**
     * 处理标的设置投标奖励处理, 并且调用存管发放红包
     *
     * @param borrow
     * @param tenderList
     */
    private void awardUserByBorrowTender(Borrow borrow, List<Tender> tenderList) throws Exception {
        Gson gson = new Gson();
        // 渠道用户投资活动触发
        for (Tender tender : tenderList) {
            UserCache userCache = userCacheService.findById(tender.getUserId());
            Users user = userService.findById(tender.getUserId());
            if ((!borrow.isTransfer()) && (!userCache.getTenderTuijian()) && (!userCache.getTenderQudao())) {
                //首次投资推荐标满2000元赠送流
                ImmutableSet channelSet = ImmutableSet.of(3, 5, 7);
                if ((!channelSet.contains(tender.getSource())) && tender.getValidMoney() >= 2000 * 100) {
                } else if ((user.getSource() == 5) && (tender.getValidMoney() >= 1000 * 100)) {
                    log.info(String.format("触发投资送流量券活动: %s", gson.toJson(tender)));
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_ACTIVITY);
                    mqConfig.setTag(MqTagEnum.GIVE_COUPON);
                    ImmutableMap<String, String> body = ImmutableMap
                            .of(MqConfig.MSG_TENDER_ID, StringHelper.toString(tender.getId()),
                                    MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
                    mqConfig.setMsg(body);
                    try {
                        log.info(String.format("borrowBizImpl firstVerify send mq %s", GSON.toJson(body)));
                        mqHelper.convertAndSend(mqConfig);
                    } catch (Throwable e) {
                        log.error("borrowBizImpl firstVerify send mq exception", e);
                    }
                }
            }
        }
    }


    /**
     * 添加用户回款计划
     *
     * @param borrow     标的信息
     * @param tenderList 投标记录
     * @param borrowDate 计算利息开始时间
     * @param groupSeqNo
     */
    private void generateBorrowCollectionAndAssetChange(Borrow borrow, List<Tender> tenderList, Date borrowDate, String groupSeqNo) throws Exception {
        Gson gson = new Gson();
        Date nowDate = new Date();
        log.info(String.format("生成用户回款计划开始: %s", gson.toJson(tenderList)));
        for (Tender tender : tenderList) {
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                    NumberHelper.toDouble(StringHelper.toString(tender.getValidMoney())),
                    NumberHelper.toDouble(StringHelper.toString(borrow.getApr())), borrow.getTimeLimit(), borrowDate);
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");
            Preconditions.checkNotNull(repayDetailList, "生成用户回款计划开始: 计划生成为空");
            BorrowCollection borrowCollection;
            int collectionMoney = 0;
            int collectionInterest = 0;
            for (int i = 0; i < repayDetailList.size(); i++) {
                borrowCollection = new BorrowCollection();
                Map<String, Object> repayDetailMap = repayDetailList.get(i);
                collectionMoney += NumberHelper.toLong(repayDetailMap.get("repayMoney"));
                collectionInterest += NumberHelper.toLong(repayDetailMap.get("interest"));
                borrowCollection.setTenderId(tender.getId());
                borrowCollection.setStatus(0);
                borrowCollection.setOrder(i);
                borrowCollection.setUserId(tender.getUserId());
                borrowCollection.setStartAt(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : borrowDate);
                borrowCollection.setStartAtYes(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : nowDate);
                borrowCollection.setCollectionAt(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
                borrowCollection.setCollectionMoney(NumberHelper.toLong(repayDetailMap.get("repayMoney")));
                borrowCollection.setPrincipal(NumberHelper.toLong(repayDetailMap.get("principal")));
                borrowCollection.setInterest(NumberHelper.toLong(repayDetailMap.get("interest")));
                borrowCollection.setCreatedAt(nowDate);
                borrowCollection.setUpdatedAt(nowDate);
                borrowCollection.setCollectionMoneyYes(0l);
                borrowCollection.setLateDays(0);
                borrowCollection.setLateInterest(0l);
                borrowCollection.setBorrowId(borrow.getId());
                borrowCollectionService.insert(borrowCollection);
            }

            // 新版投标成功
            AssetChange tenderAssetChangeEntity = new AssetChange();
            tenderAssetChangeEntity.setSourceId(tender.getId());
            tenderAssetChangeEntity.setGroupSeqNo(groupSeqNo);
            tenderAssetChangeEntity.setMoney(tender.getValidMoney());
            tenderAssetChangeEntity.setSeqNo(assetChangeProvider.getSeqNo());
            tenderAssetChangeEntity.setRemark(String.format("成功投资标的[%s], 扣除资金%s元", borrow.getName(), StringHelper.formatDouble(tender.getValidMoney() / 100D, true)));
            tenderAssetChangeEntity.setType(AssetChangeTypeEnum.tender);
            tenderAssetChangeEntity.setUserId(tender.getUserId());
            tenderAssetChangeEntity.setForUserId(ObjectUtils.isArray(borrow.getTakeUserId()) ? borrow.getUserId() : borrow.getTakeUserId());
            assetChangeProvider.commonAssetChange(tenderAssetChangeEntity);

            // 添加待收
            AssetChange collectionAddChangeEntity = new AssetChange();
            collectionAddChangeEntity.setSourceId(tender.getId());
            collectionAddChangeEntity.setGroupSeqNo(groupSeqNo);
            collectionAddChangeEntity.setMoney(collectionMoney);
            collectionAddChangeEntity.setInterest(collectionInterest);
            collectionAddChangeEntity.setSeqNo(assetChangeProvider.getSeqNo());
            collectionAddChangeEntity.setRemark(String.format("成功投资标的[%s],添加待收%s元", borrow.getName(), StringHelper.formatDouble(collectionMoney / 100D, true)));
            collectionAddChangeEntity.setType(AssetChangeTypeEnum.collectionAdd);
            collectionAddChangeEntity.setUserId(tender.getUserId());
            collectionAddChangeEntity.setForUserId(tender.getUserId());
            assetChangeProvider.commonAssetChange(collectionAddChangeEntity);
        }
    }

    /**
     * 更改原始债权信息
     * 1. 更改原始债权投标记录为已转让
     * 2. 更改原始债权待收记录为已转让标至
     * 3. 减少债权人待收金额
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    private List<BorrowCollection> processOldTenderAndBorrowCollection(Borrow borrow) throws Exception {
        Long oldTenderId = borrow.getTenderId();  // 原标的投标记录

        //将债权转人待收设置为已转让状态
        BorrowCollection borrowCollection = new BorrowCollection();
        borrowCollection.setTransferFlag(1);
        Specification<BorrowCollection> bcs = Specifications.<BorrowCollection>and()
                .eq("tenderId", oldTenderId)
                .eq("status", 0)
                .build();
        borrowCollectionService.updateBySpecification(borrowCollection, bcs);
        // 更新装让人投标记录为转让状态
        Tender tender = tenderService.findById(oldTenderId);
        tender.setId(oldTenderId);
        tender.setTransferFlag(2);
        tenderService.updateById(tender);

        //======================================================================
        // 扣除转让待收
        bcs = Specifications.<BorrowCollection>and()
                .eq("tenderId", oldTenderId)
                .eq("status", 0)
                .eq("transferFlag", 1)
                .build();
        List<BorrowCollection> oldBorrowCollections = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "order"));
        Preconditions.checkNotNull(oldBorrowCollections, "债权转让复审: 原始投标还款计划为空");
        long collectionMoney = oldBorrowCollections.stream().mapToLong(borrowCollection1 -> borrowCollection1.getCollectionMoney()).sum();  // 待收
        long collectionInterest = oldBorrowCollections.stream().mapToLong(borrowCollection1 -> borrowCollection1.getInterest()).sum(); //  待收利息
        CapitalChangeEntity entity = new CapitalChangeEntity();
        entity.setType(CapitalChangeEnum.CollectionLower);
        entity.setUserId(borrow.getUserId());
        entity.setMoney(collectionMoney);
        entity.setInterest(collectionInterest);
        entity.setRemark("债权转让成功，扣除待收资金");
        capitalChangeHelper.capitalChange(entity);
        return oldBorrowCollections;
    }

    /**
     * 投资车贷标成功添加 自身车贷标待收本金 和 推荐人的邀请用户车贷标总待收本金
     * 更新 投过相应标种 标识
     *
     * @param tender
     * @param borrow
     * @param repayDetailList
     */
    private Map<String, Object> updateUserCacheByTenderSuccess(Tender tender, Borrow borrow, List<Map<String, Object>> repayDetailList) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        Users user = userService.findByIdLock(tender.getUserId());
        UserCache userCache = userCacheService.findByUserIdLock(tender.getUserId());
        if ((!borrow.isTransfer()) && (!userCache.getTenderTuijian()) && (!userCache.getTenderQudao())) {
            //首次投资推荐标满2000元赠送流
            ImmutableSet channelSet = ImmutableSet.of(3, 5, 7);
            if ((!channelSet.contains(tender.getSource())) && tender.getValidMoney() >= 2000 * 100) {
            } else if ((user.getSource() == 5) && (tender.getValidMoney() >= 1000 * 100)) {
                MqConfig mqConfig = new MqConfig();
                mqConfig.setQueue(MqQueueEnum.RABBITMQ_ACTIVITY);
                mqConfig.setTag(MqTagEnum.GIVE_COUPON);
                ImmutableMap<String, String> body = ImmutableMap
                        .of(MqConfig.MSG_TENDER_ID, StringHelper.toString(tender.getId()),
                                MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
                mqConfig.setMsg(body);
                try {
                    log.info(String.format("borrowBizImpl firstVerify send mq %s", GSON.toJson(body)));
                    mqHelper.convertAndSend(mqConfig);
                } catch (Throwable e) {
                    log.error("borrowBizImpl firstVerify send mq exception", e);
                }
            }
        }

        long countInterest = 0;
        for (int i = 0; i < repayDetailList.size(); i++) {
            Map<String, Object> repayDetailMap = repayDetailList.get(i);
            countInterest += NumberHelper.toLong(repayDetailMap.get("interest"));
        }

        UserCache tempUserCache = new UserCache();
        if (borrow.getType() == 0) {
            tempUserCache.setTjWaitCollectionPrincipal(userCache.getTjWaitCollectionPrincipal() + tender.getValidMoney());
            tempUserCache.setTjWaitCollectionInterest(userCache.getTjWaitCollectionInterest() + countInterest);
        }

        if (borrow.getType() == 4) {
            tempUserCache.setQdWaitCollectionPrincipal(userCache.getQdWaitCollectionPrincipal() + tender.getValidMoney());
            tempUserCache.setQdWaitCollectionInterest(userCache.getQdWaitCollectionInterest() + countInterest);
        }

        try {
            IncrStatistic incrStatistic = new IncrStatistic();
            if ((!userCache.getTenderTransfer()) && (!userCache.getTenderTuijian()) && (!userCache.getTenderJingzhi()) && (!userCache.getTenderMiao()) && (!userCache.getTenderQudao())) {
                incrStatistic.setTenderCount(1);
                incrStatistic.setTenderTotal(1);
            }

            if (borrow.isTransfer() && (!userCache.getTenderTransfer())) {
                tempUserCache.setTenderTransfer(true);
                incrStatistic.setTenderLzCount(1);
                incrStatistic.setTenderLzTotalCount(1);
            } else if ((borrow.getType() == 0) && (!userCache.getTenderTuijian())) {
                tempUserCache.setTenderTuijian(true);
                incrStatistic.setTenderTjCount(1);
                incrStatistic.setTenderTjTotalCount(1);
            } else if ((borrow.getType() == 1) && (!userCache.getTenderJingzhi())) {
                tempUserCache.setTenderJingzhi(true);
                incrStatistic.setTenderJzCount(1);
                incrStatistic.setTenderJzTotalCount(1);
            } else if ((borrow.getType() == 2) && (!userCache.getTenderMiao())) {
                tempUserCache.setTenderMiao(true);
                incrStatistic.setTenderMiaoCount(1);
                incrStatistic.setTenderMiaoTotalCount(1);
            } else if ((borrow.getType() == 4) && (!userCache.getTenderQudao())) {
                tempUserCache.setTenderQudao(true);
                incrStatistic.setTenderQdCount(1);
                incrStatistic.setTenderQdTotalCount(1);
            }
            if (!ObjectUtils.isEmpty(incrStatistic)) {
                incrStatisticBiz.caculate(incrStatistic);
            }
        } catch (Throwable e) {
            log.error(String.format("投标成功统计错误：%s", e.getMessage()));
        }

        //======================================
        // 老用户投标红包
        //======================================


        //======================================
        // 推荐用户投资红包
        //======================================

        return resultMap;
    }

    /**
     * 检查提前结清参数
     *
     * @param voRepayAll
     * @return
     */
    public ResponseEntity<VoBaseResp> checkRepayAll(com.gofobao.framework.borrow.vo.request.VoRepayAll voRepayAll) {
        Long borrowId = voRepayAll.getBorrowId();
        Borrow borrow = borrowService.findByIdLock(borrowId);
        if ((borrow.getStatus() != 3) || (borrow.getType() != 0 && borrow.getType() != 4)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款状态非可结清状态！"));
        }

        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("borrowId", borrowId)
                .eq("status", 0)
                .build();
        if (borrowRepaymentService.count(brs) < 1) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "该借款剩余未还期数小于1期！"));
        }
        return null;
    }

    /**
     * 提前结清
     *
     * @param voRepayAll
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> repayAll(com.gofobao.framework.borrow.vo.request.VoRepayAll voRepayAll) {

        ResponseEntity resp = checkRepayAll(voRepayAll);
        if (!ObjectUtils.isEmpty(resp)) {
            return resp;
        }

        Long borrowId = voRepayAll.getBorrowId();
        Borrow borrow = borrowService.findByIdLock(borrowId);
        Asset borrowAsset = assetService.findByUserId(borrow.getUserId());
        Preconditions.checkNotNull(borrowAsset, "借款人资产记录不存在!");

        int repaymentTotal = 0;
        List<VoRepayReq> voRepayReqList = new ArrayList<>();
        long penalty = 0;
        int lateInterest = 0;
        int lateDays = 0;
        int overPrincipal = 0;
        Date startAt = null;
        Date endAt = null;
        BorrowRepayment borrowRepayment = null;
        double interestPercent = 0;
        VoRepayReq voRepayReq = null;
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("borrowId", borrowId)
                .eq("status", 0)
                .build();
        List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);

        for (int i = 0; i < borrowRepaymentList.size(); i++) {
            borrowRepayment = borrowRepaymentList.get(i);

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
            } else {
                lateInterest = 0;
            }
            repaymentTotal += borrowRepayment.getPrincipal() + borrowRepayment.getInterest() * interestPercent + lateInterest;
            voRepayReq = new VoRepayReq();
            voRepayReq.setInterestPercent(interestPercent);
            voRepayReq.setRepaymentId(borrowRepayment.getId());
            voRepayReq.setUserId(borrowRepayment.getUserId());
            voRepayReq.setIsUserOpen(false);
            voRepayReqList.add(voRepayReq);
        }

        long repayMoney = repaymentTotal + penalty;
        if (borrowAsset.getUseMoney() < (repayMoney)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "结清总共需要还款 " + repayMoney + " 元，您的账户余额不足，请先充值!！"));
        }

        for (VoRepayReq tempVoRepayReq : voRepayReqList) {
            try {
                repaymentBiz.repayDeal(tempVoRepayReq);
            } catch (Throwable e) {
                log.error("提前结清异常：", e);
            }
        }

        if (penalty > 0) {
            CapitalChangeEntity entity = new CapitalChangeEntity();
            entity.setUserId(borrow.getUserId());
            entity.setType(CapitalChangeEnum.Fee);
            entity.setMoney(penalty);
            entity.setRemark("扣除提前结清的违约金");
            try {
                capitalChangeHelper.capitalChange(entity);
                receivedPenalty(borrow, penalty);
            } catch (Throwable e) {
                log.error("BorrowBizImpl 异常:", e);
            }
        }

        return ResponseEntity.ok(VoBaseResp.ok("提前结清成功!"));
    }

    /**
     * 提前结清给投资者违约金
     *
     * @param borrow
     * @param penalty
     */
    private void receivedPenalty(Borrow borrow, long penalty) throws Exception {
        Date nowDate = new Date();
        List<Long> collectionUserIds = new ArrayList<>();
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .eq("status", 1)
                .build();
        Pageable pageable = null;
        List<Tender> tenderList = null;
        int pageNum = 0;
        int pageSize = 10;
        int tempPenalty = 0;
        Borrow tempBorrow = null;
        long tenderUserId = 0;
        UserThirdAccount tenderUserThirdAccount = null;
        do {
            pageable = new PageRequest(pageNum++, pageSize, new Sort(Sort.Direction.ASC));
            tenderList = tenderService.findList(ts, pageable);
            for (Tender tender : tenderList) {
                tenderUserId = tender.getUserId();
                tempPenalty = (int) MathHelper.myRound(tender.getValidMoney().doubleValue() / borrow.getMoney().doubleValue() * penalty, 0);
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

                //查询红包账户
                DictValue dictValue = jixinCache.get(JixinContants.RED_PACKET_USER_ID);
                UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(NumberHelper.toLong(dictValue.getValue03()));

                tenderUserThirdAccount = userThirdAccountService.findByUserId(tenderUserId);
                //调用即信发送红包接口
                VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                voucherPayRequest.setTxAmount(StringHelper.formatDouble(tempPenalty, 100, false));
                voucherPayRequest.setForAccountId(tenderUserThirdAccount.getAccountId());
                voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                voucherPayRequest.setDesLine("借款'" + borrow.getName() + "'的违约金");
                voucherPayRequest.setChannel(ChannelContant.HTML);
                VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                    String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                    throw new Exception(msg);
                }

                CapitalChangeEntity entity = new CapitalChangeEntity();
                entity.setUserId(tenderUserId);
                entity.setType(CapitalChangeEnum.IncomeOther);
                entity.setMoney(tempPenalty);
                entity.setRemark("收到借款用户提前结清的违约金");
                capitalChangeHelper.capitalChange(entity);

                if (!collectionUserIds.contains(tenderUserId)) {
                    collectionUserIds.add(tenderUserId);
                    Notices notices = new Notices();
                    notices.setFromUserId(1L);
                    notices.setUserId(tenderUserId);
                    notices.setRead(false);
                    notices.setName("违约金");
                    notices.setContent("客户在" + DateHelper.dateToString(new Date()) + "已将借款[" + BorrowHelper.getBorrowLink(borrow.getId(), borrow.getName()) + "]]提前结清，收到" + tempPenalty + "元违约金");
                    notices.setType("system");
                    notices.setCreatedAt(nowDate);
                    notices.setUpdatedAt(nowDate);

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
            }
        } while (tenderList.size() < 10);
    }

    /**
     * 请求复审
     */
    public ResponseEntity<VoBaseResp> doAgainVerify(VoDoAgainVerifyReq voDoAgainVerifyReq) {
        String paramStr = voDoAgainVerifyReq.getParamStr();
        if (!SecurityHelper.checkSign(voDoAgainVerifyReq.getSign(), paramStr)) {
            log.error("BorrowBizImpl doAgainVerify error：签名校验不通过");
        }

        Map<String, String> paramMap = GSON.fromJson(paramStr, new TypeToken<Map<String, String>>() {
        }.getType());
        boolean flag = false;
        try {
            flag = borrowProvider.doAgainVerify(paramMap);
        } catch (Throwable e) {
            log.error("PC 复审异常", e);
        }
        return ResponseEntity.ok(VoBaseResp.ok(StringHelper.toString(flag)));
    }

    /**
     * 登记官方借款（车贷标、渠道标）
     *
     * @param voRegisterOfficialBorrow
     * @param request
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoHtmlResp> registerOfficialBorrow(VoRegisterOfficialBorrow voRegisterOfficialBorrow, HttpServletRequest request) {
        log.info(String.format("车贷标/ 渠道标初审: 请求信息( %s )", GSON.toJson(voRegisterOfficialBorrow)));
        String paramStr = voRegisterOfficialBorrow.getParamStr();
        if (!SecurityHelper.checkSign(voRegisterOfficialBorrow.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc 登记官方借款 签名验证不通过", VoHtmlResp.class));
        }

        Map<String, String> paramMap = GSON.fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long borrowId = NumberHelper.toLong(paramMap.get("borrowId"));
        Borrow borrow = borrowService.findById(borrowId);
        Preconditions.checkNotNull(borrow, "当前标的信息为空");
        Long userId = borrow.getUserId();

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
        if (ObjectUtils.isEmpty(userThirdAccount)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款人未开通存管账户!", VoHtmlResp.class));
        }

        if (!userThirdAccount.getPasswordState().equals(1)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "借款人还未初始化银行交易密码!", VoHtmlResp.class));
        }

        if (!borrowThirdBiz.registerBorrrowConditionCheck(borrow)) {
            VoCreateThirdBorrowReq voCreateThirdBorrowReq = new VoCreateThirdBorrowReq();
            voCreateThirdBorrowReq.setBorrowId(borrowId);
            voCreateThirdBorrowReq.setEntrustFlag(true);
            ResponseEntity<VoBaseResp> resp = borrowThirdBiz.createThirdBorrow(voCreateThirdBorrowReq);   // 即信标的登记
            if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) { //创建状态为失败时返回错误提示
                log.error(String.format("车贷标/ 渠道标初审: 存管登记失败( %s )", GSON.toJson(voRegisterOfficialBorrow)));
                return ResponseEntity
                        .badRequest()
                        .body(VoHtmlResp.error(VoHtmlResp.ERROR, resp.getBody().getState().getMsg(), VoHtmlResp.class));
            }
            log.info(String.format("车贷标/ 渠道标初审: 存管登记成功( %s )", GSON.toJson(voRegisterOfficialBorrow)));
        }

        //受托支付
        if (!ObjectUtils.isEmpty(borrow.getTakeUserId())) {
            VoThirdTrusteePayReq voThirdTrusteePayReq = new VoThirdTrusteePayReq();
            voThirdTrusteePayReq.setBorrowId(borrowId);
            return borrowThirdBiz.thirdTrusteePay(voThirdTrusteePayReq, request);
        } else {
            return ResponseEntity.ok(VoBaseResp.ok("初审成功", VoHtmlResp.class));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doTrusteePay(Long borrowId) {
        Borrow borrow = borrowService.findByIdLock(borrowId);
        String productId = borrow.getProductId();
        Preconditions.checkNotNull(productId, "受托支付记录查询, 当前标的为登记");
        Long userId = borrow.getUserId();
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);

        TrusteePayQueryReq trusteePayQueryReq = new TrusteePayQueryReq();
        trusteePayQueryReq.setChannel(ChannelContant.HTML);
        trusteePayQueryReq.setAccountId(userThirdAccount.getAccountId());
        trusteePayQueryReq.setProductId(productId);
        TrusteePayQueryResp trusteePayQueryResp = jixinManager.send(JixinTxCodeEnum.TRUSTEE_PAY_QUERY, trusteePayQueryReq, TrusteePayQueryResp.class);
        if ((ObjectUtils.isEmpty(trusteePayQueryResp))
                || !(JixinResultContants.SUCCESS.equals(trusteePayQueryResp.getRetCode()))) {
            return false;
        }

        if (!trusteePayQueryResp.getState().equals("1")) {
            return false;
        }

        // 确认后初审
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_BORROW);
        mqConfig.setTag(MqTagEnum.FIRST_VERIFY);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrowId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("borrowBizImpl firstVerify send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("borrowBizImpl firstVerify send mq exception", e);
        }
        return true;
    }

    /**
     * 发送借款协议
     *
     * @param borrow
     */
    public void sendBorrowProtocol(Borrow borrow) {
        List<Tender> tenderList = null;
        Users borrowUser = null;
        List<Users> tenderUserList = null;
        Map<String, Object> borrowMap = null;
        List<Map<String, Object>> tenderMapList = null;
        Map<String, Object> calculatorMap = null;
        String content = null;
        String username = null;

        if (!ObjectUtils.isEmpty(borrow)) {

            //查询借款信息

            borrowMap = GSON.fromJson(GSON.toJson(borrow), new com.google.common.reflect.TypeToken<Map<String, Object>>() {
            }.getType());
            borrowUser = userService.findById(borrow.getUserId());
            username = borrowUser.getUsername();

            borrowMap.put("username", org.apache.commons.lang3.StringUtils.isEmpty(username) ? borrowUser.getPhone() : username);
            borrowMap.put("cardId", UserHelper.hideChar(borrowUser.getCardId(), UserHelper.CARD_ID_NUM));

            if (!ObjectUtils.isEmpty(borrow.getSuccessAt())) { //判断是否存在满标时间
                boolean successAtBool = DateHelper.getMonth(DateHelper.addMonths(borrow.getSuccessAt(), borrow.getTimeLimit())) % 12
                        !=
                        (DateHelper.getMonth(borrow.getSuccessAt()) + borrow.getTimeLimit()) % 12;

                String borrowExpireAtStr = null;
                String monthAsReimbursement = null;//月截止还款日
                if (borrow.getRepayFashion() == 1) {
                    borrowExpireAtStr = DateHelper.dateToString(DateHelper.addDays(borrow.getSuccessAt(), borrow.getTimeLimit()), "yyyy-MM-dd");
                    monthAsReimbursement = borrowExpireAtStr;
                } else {
                    if (successAtBool) {
                        borrowExpireAtStr = DateHelper.dateToString(DateHelper.subDays(DateHelper.addDays(DateHelper.setDays(borrow.getSuccessAt(), borrow.getTimeLimit()), 1), 1), "yyyy-MM-dd HH:mm:ss");
                    } else {
                        borrowExpireAtStr = DateHelper.dateToString(DateHelper.addMonths(borrow.getSuccessAt(), borrow.getTimeLimit()), "yyyy-MM-dd");
                    }
                    monthAsReimbursement = "每月" + DateHelper.getDay(borrow.getSuccessAt()) + "日";
                }
                borrowMap.put("borrowExpireAtStr", borrowExpireAtStr);
                borrowMap.put("monthAsReimbursement", monthAsReimbursement);
            }


            //使用当前借款计算利息信息
            BorrowCalculatorHelper borrowCalculatorHelper = null;

            //查询投标信息
            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .eq("borrowId", borrow.getId())
                    .build();

            tenderList = tenderService.findList(ts);

            if (!CollectionUtils.isEmpty(tenderList)) {
                List<Long> tenderUserIds = new ArrayList<>();

                tenderMapList = GSON.fromJson(GSON.toJson(tenderList), new com.google.common.reflect.TypeToken<List<Map<String, Object>>>() {
                }.getType());

                for (Tender tempTender : tenderList) {
                    tenderUserIds.add(tempTender.getUserId());
                }

                Specification<Users> us = Specifications
                        .<Users>and()
                        .in("id", tenderUserIds.toArray())
                        .build();

                tenderUserList = userService.findList(us);

                List<Map<String, Object>> tempTenderMapList = null;
                Map<String, String> msgMap = new HashMap<>();
                Users tenderUser = null;
                for (Map<String, Object> tempTenderMap : tenderMapList) {
                    tempTenderMapList = new ArrayList<>();

                    for (Users tempTenderUser : tenderUserList) {
                        if (NumberHelper.toInt(tempTenderMap.get("userId")) == NumberHelper.toInt(tempTenderUser.getId())) {
                            tenderUser = tempTenderUser;
                            break;
                        }
                    }

                    if (ObjectUtils.isEmpty(tenderUser.getEmail())) {
                        continue;
                    }

                    borrowCalculatorHelper = new BorrowCalculatorHelper(NumberHelper.toDouble(tempTenderMap.get("validMoney")), new Double(borrow.getApr()), borrow.getTimeLimit(), null);
                    calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
                    tempTenderMap.put("calculatorMap", calculatorMap);

                    username = tenderUser.getUsername();
                    tempTenderMap.put("username", org.apache.commons.lang3.StringUtils.isEmpty(username) ? tenderUser.getPhone() : username);

                    tempTenderMapList.add(tempTenderMap);

                    //使用thymeleaf模版引擎渲染 借款合同html
                    Map<String, Object> templateMap = new HashMap<>();
                    templateMap.put("borrowMap", borrowMap);
                    templateMap.put("tenderMapList", tempTenderMapList);
                    templateMap.put("calculatorMap", calculatorMap);
                    content = thymeleafHelper.build("borrowProtocol", templateMap);

                    // 使用消息队列发送邮件
                    MqConfig config = new MqConfig();
                    config.setQueue(MqQueueEnum.RABBITMQ_EMAIL);
                    config.setTag(MqTagEnum.SEND_BORROW_PROTOCOL_EMAIL);
                    ImmutableMap<String, String> body = ImmutableMap
                            .of(MqConfig.EMAIL, tenderUser.getEmail(),
                                    MqConfig.IP, "127.0.0.1",
                                    "subject", "广富宝金服借款协议",
                                    "content", content);
                    config.setMsg(body);
                    mqHelper.convertAndSend(config);

                }
            }
        }
    }

    /**
     * 借款成功发送通知短信
     *
     * @param borrow
     * @throws Exception
     */
    private void smsNoticeByBorrowReview(Borrow borrow) throws Exception {
        Users user = userService.findById(borrow.getUserId());
        if ((borrow.getType() == 1) && (!ObjectUtils.isEmpty(borrow.getLendId())) && ((borrow.getApr() / 100) > 1)
                && ((borrow.getRepayFashion() != 1) || (borrow.getTimeLimit() > 1))) {
            String phone = user.getPhone();
            if (!ObjectUtils.isEmpty(phone)) {
                long fee = 0;
                if (borrow.getRepayFashion() == 1) {
                    fee = Math.round(borrow.getMoney() * 0.12 / 30 * borrow.getTimeLimit());
                } else {
                    fee = Math.round(borrow.getMoney() * 0.12 * borrow.getTimeLimit());
                }
                // 使用消息队列发送短信
                MqConfig config = new MqConfig();
                config.setQueue(MqQueueEnum.RABBITMQ_SMS);
                config.setTag(MqTagEnum.SMS_BORROW_SUCCESS);
                ImmutableMap<String, String> body = ImmutableMap
                        .of(MqConfig.PHONE, phone,
                                MqConfig.IP, "127.0.0.1",
                                "money", StringHelper.formatDouble(borrow.getMoney(), 100.0, true),
                                "fee", StringHelper.formatDouble(fee, 100.0, true),
                                "id", StringHelper.toString(borrow.getId()));
                config.setMsg(body);
                mqHelper.convertAndSend(config);
            }
        }
    }

    /**
     * 如果是流转标则扣除 自身车贷标待收本金 和 推荐人的邀请用户车贷标总待收本金
     *
     * @param borrow
     */
    private void updateUserCacheByBorrowReview(Borrow borrow) throws Exception {
        UserCache userCache = userCacheService.findById(borrow.getUserId());
        if (borrow.isTransfer()) {
            Specification<BorrowCollection> bcs = Specifications
                    .<BorrowCollection>and()
                    .eq("status", 0)
                    .eq("tenderId", borrow.getTenderId())
                    .build();

            List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
            if (CollectionUtils.isEmpty(borrowCollectionList)) {
                return;
            }
            long countInterest = 0;
            for (BorrowCollection borrowCollection : borrowCollectionList) {
                countInterest += borrowCollection.getInterest();
            }
            userCache.setUserId(userCache.getUserId());
            if (borrow.getType() == 0) {
                userCache.setTjWaitCollectionPrincipal(userCache.getTjWaitCollectionPrincipal() - borrow.getMoney());
                userCache.setTjWaitCollectionInterest(userCache.getTjWaitCollectionInterest() - countInterest);
            } else if (borrow.getType() == 4) {
                userCache.setQdWaitCollectionPrincipal(userCache.getQdWaitCollectionPrincipal() - borrow.getMoney());
                userCache.setQdWaitCollectionInterest(userCache.getQdWaitCollectionInterest() - countInterest);
            }
            userCacheService.save(userCache);
        }
    }

    /**
     * 更新网站统计
     *
     * @param borrow
     */
    private void updateStatisticByBorrowReview(Borrow borrow) {
        Date nowDate = new Date();
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("borrowId", borrow.getId())
                .build();

        List<BorrowRepayment> repaymentList = borrowRepaymentService.findList(brs);
        if (CollectionUtils.isEmpty(repaymentList)) {//查询当前借款 还款记录
            return;
        }

        long repayMoney = 0;
        long principal = 0;
        for (BorrowRepayment borrowRepayment : repaymentList) {
            repayMoney += borrowRepayment.getRepayMoney();
            principal += borrowRepayment.getPrincipal();
        }

        //全站统计
        Statistic statistic = new Statistic();
        long borrowMoney = borrow.getMoney();

        statistic.setBorrowItems(1L);
        statistic.setBorrowTotal((long) borrowMoney);
        statistic.setWaitRepayTotal((long) repayMoney);

        if (borrow.isTransfer()) {
            statistic.setLzBorrowTotal((long) borrowMoney);
        } else if (borrow.getType() == 0) {//0：车贷标；1：净值标；2：秒标；4：渠道标；
            statistic.setTjBorrowTotal((long) borrowMoney);
            statistic.setTjWaitRepayPrincipalTotal((long) principal);
            statistic.setTjWaitRepayTotal((long) repayMoney);
        } else if (borrow.getType() == 1) {
            statistic.setJzBorrowTotal((long) borrowMoney);
            statistic.setJzWaitRepayPrincipalTotal((long) principal);
            statistic.setJzWaitRepayTotal((long) repayMoney);
        } else if (borrow.getType() == 2) {
            statistic.setMbBorrowTotal((long) borrowMoney);
        } else if (borrow.getType() == 4) {
            statistic.setQdBorrowTotal((long) borrowMoney);
            statistic.setQdWaitRepayPrincipalTotal((long) principal);
            statistic.setQdWaitRepayTotal((long) repayMoney);
        }
        if (!ObjectUtils.isEmpty(statistic)) {
            try {
                statisticBiz.caculate(statistic);
            } catch (Throwable e) {
                log.error("borrowProvider updateStatisticByBorrowReview 异常:", e);
            }
        }
    }

    /**
     * 初审
     *
     * @param borrowId
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doFirstVerify(Long borrowId) throws Exception {
        Borrow borrow = borrowService.findByIdLock(borrowId);
        if ((ObjectUtils.isEmpty(borrow)) || (borrow.getStatus() != 0)) {
            log.error("标的初审重复审核!");
            return false;
        }

        Gson gson = new Gson();
        if (!ObjectUtils.isEmpty(borrow.getLendId())) {
            log.info(String.format("有草出借标的初步审核: %s", gson.toJson(borrow)));
            return verifyLendBorrow(borrow);      //有草出借初审
        } else {
            log.info(String.format("常规标的初步审核: %s", gson.toJson(borrow)));
            return verifyStandardBorrow(borrow);  //标准标的初审
        }

    }


    /**
     * 车贷标、净值标、渠道标、转让标初审
     * 标的状态改变
     * 判断是否需要推送到自动投标队列
     *
     * @return
     */
    private boolean verifyStandardBorrow(Borrow borrow) {
        Date nowDate = DateHelper.subSeconds(new Date(), 10);
        borrow.setStatus(1);
        borrow.setVerifyAt(nowDate);
        Date releaseAt = borrow.getReleaseAt();
        borrow.setReleaseAt(ObjectUtils.isEmpty(releaseAt) ? nowDate : releaseAt);
        borrow = borrowService.save(borrow);    //更新借款状态
        if (!borrowThirdBiz.registerBorrrowConditionCheck(borrow)) { // 判断没有在即信注册、并且类型为非转让标
            int type = borrow.getType();
            if (type != 0 && type != 4) { // 判断是否是官标、官标不需要在这里登记标的
                VoCreateThirdBorrowReq voCreateThirdBorrowReq = new VoCreateThirdBorrowReq();
                voCreateThirdBorrowReq.setBorrowId(borrow.getId());
                ResponseEntity<VoBaseResp> resp = borrowThirdBiz.createThirdBorrow(voCreateThirdBorrowReq);
                if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) { //创建状态为失败时返回错误提示
                    log.error(String.format("标的初审: 普通标的报备 %s", new Gson().toJson(resp)));
                    return false;
                }
            }
        }

        // 自动投标前提:
        // 1.没有设置标密码
        // 2.车贷标, 渠道标, 流转表
        // 3.标的年化率为 800 以上
        Integer borrowType = borrow.getType();
        ImmutableList<Integer> autoTenderBorrowType = ImmutableList.of(0, 1, 4);
        if ((ObjectUtils.isEmpty(borrow.getPassword()))
                && (autoTenderBorrowType.contains(borrowType)) && borrow.getApr() > 800) {
            borrow.setIsLock(true);
            borrowService.updateById(borrow);  // 锁住标的,禁止手动投标
            if (borrow.getIsNovice()) {   // 对于新手标直接延迟8点后推送
                Date noviceBorrowStandeReaseAt = DateHelper.addHours(DateHelper.beginOfDate(new Date()), 20);  // 新手标 能进行制动的时间
                releaseAt = DateHelper.max(noviceBorrowStandeReaseAt, releaseAt);
            }

            //触发自动投标队列
            MqConfig mqConfig = new MqConfig();
            mqConfig.setQueue(MqQueueEnum.RABBITMQ_TENDER);
            mqConfig.setTag(MqTagEnum.AUTO_TENDER);
            mqConfig.setSendTime(releaseAt);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(borrow.getId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
            mqConfig.setMsg(body);
            try {
                log.info(String.format("borrowProvider autoTender send mq %s", GSON.toJson(body)));
                mqHelper.convertAndSend(mqConfig);
                return true;
            } catch (Throwable e) {
                log.error("borrowProvider autoTender send mq exception", e);
                return false;
            }
        }


        return true;
    }

    /**
     * 摘草 生成借款 初审
     * <p>
     * 更改标的为可投状态,
     * 存管平台报备
     * 并且调用投标流程, 完成摘草动作
     *
     * @param borrow
     * @return
     * @throws Exception
     */
    private boolean verifyLendBorrow(Borrow borrow) throws Exception {
        Date nowDate = DateHelper.subSeconds(new Date(), 10);
        borrow.setStatus(1);  //更新借款状态
        borrow.setVerifyAt(nowDate);
        Date releaseAt = borrow.getReleaseAt();
        borrow.setReleaseAt(ObjectUtils.isEmpty(releaseAt) ? nowDate : releaseAt);
        borrow = borrowService.save(borrow);// 更改标的为可投标状态
        if (!borrowThirdBiz.registerBorrrowConditionCheck(borrow)) { // 判断没有在即信注册、并且类型为非转让标
            VoCreateThirdBorrowReq voCreateThirdBorrowReq = new VoCreateThirdBorrowReq();
            voCreateThirdBorrowReq.setBorrowId(borrow.getId());
            ResponseEntity<VoBaseResp> resp = borrowThirdBiz.createThirdBorrow(voCreateThirdBorrowReq);
            if (resp.getBody().getState().getCode() == VoBaseResp.ERROR) { //创建状态为失败时返回错误提示
                log.error(String.format("标的初审: 摘草报备标的信息失败 %s", new Gson().toJson(resp)));
                return false;
            }
        }

        Long lendId = borrow.getLendId();
        Lend lend = lendService.findById(lendId);
        VoCreateTenderReq voCreateTenderReq = new VoCreateTenderReq();
        voCreateTenderReq.setUserId(lend.getUserId());
        voCreateTenderReq.setBorrowId(borrow.getId());
        voCreateTenderReq.setTenderMoney(MathHelper.myRound(borrow.getMoney() / 100.0, 2));
        ResponseEntity<VoBaseResp> response = tenderBiz.createTender(voCreateTenderReq);
        return response.getStatusCode().equals(HttpStatus.OK);
    }

    /**
     * pc初审
     *
     * @param voPcDoFirstVerity
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBaseResp> pcFirstVerify(VoPcDoFirstVerity voPcDoFirstVerity) throws Exception {
        String paramStr = voPcDoFirstVerity.getParamStr();
        if (!SecurityHelper.checkSign(voPcDoFirstVerity.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "pc去初审 签名验证不通过!"));
        }

        Map<String, String> paramMap = new Gson().fromJson(paramStr, TypeTokenContants.MAP_ALL_STRING_TOKEN);
        Long borrowId = NumberHelper.toLong(paramMap.get("borrowId"));

        boolean verifyState = doFirstVerify(borrowId); // 初审标的
        if (verifyState) {
            return ResponseEntity.ok(VoBaseResp.ok("初审成功!"));
        } else {
            return ResponseEntity.
                    badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "初审失败!"));
        }
    }

}
