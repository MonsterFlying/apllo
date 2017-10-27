package com.gofobao.framework.test;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeReq;
import com.gofobao.framework.api.model.balance_freeze.BalanceFreezeResp;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeReq;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeResp;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestReq;
import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestResp;
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvest;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.batch_query.BatchQueryReq;
import com.gofobao.framework.api.model.batch_query.BatchQueryResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayReq;
import com.gofobao.framework.api.model.batch_repay.BatchRepayResp;
import com.gofobao.framework.api.model.batch_repay.Repay;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryRequest;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResponse;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryRequest;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryResponse;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryReq;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryResp;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryRequest;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryResponse;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.contants.AssetTypeContants;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.integral.IntegralChangeEntity;
import com.gofobao.framework.common.integral.IntegralChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BatchAssetChangeHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.helper.project.BorrowHelper;
import com.gofobao.framework.helper.project.IntegralChangeHelper;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.entity.RepayAssetChange;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchDealLogContants;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gofobao.framework.helper.DateHelper.isBetween;
import static com.gofobao.framework.listener.providers.NoticesMessageProvider.GSON;
import static java.util.stream.Collectors.groupingBy;

/**
 * Created by Zeke on 2017/6/21.
 */
@RestController
@Api(description = "自动投标规则控制器")
@RequestMapping
@Slf4j
public class TestController {

    @Autowired
    private AutoTenderBiz autoTenderBiz;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AssetChangeProvider assetChangeProvider;

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private NewAssetLogService newAssetLogService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    MqHelper mqHelper;
    final Gson GSON = new GsonBuilder().create();
    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private ThirdBatchDealLogBiz thirdBatchDealLogBiz;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private TransferBiz transferBiz;
    @Autowired
    private UserService userService;
    @Autowired
    private BatchAssetChangeService batchAssetChangeService;
    @Autowired
    private BatchAssetChangeItemService batchAssetChangeItemService;
    @Value("${gofobao.javaDomain}")
    private String javaDomain;
    @Autowired
    private TransferService transferService;
    @Autowired
    private BatchAssetChangeHelper batchAssetChangeHelper;
    @Autowired
    private IntegralChangeHelper integralChangeHelper;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private TransferBuyLogService transferBuyLogService;

    /**
     * 生成回款记录
     *
     * @param borrow
     * @param borrowRepayment
     * @param repayUserId
     * @param repayAssetChanges
     * @param batchAssetChange
     */
    private long doGenerateAssetChangeRecodeByRepay(Borrow borrow, Map<Long, Tender> tenderMap, BorrowRepayment borrowRepayment, long repayUserId, List<RepayAssetChange> repayAssetChanges, String groupSeqNo, BatchAssetChange batchAssetChange, boolean advance) throws ExecutionException {
        long batchAssetChangeId = batchAssetChange.getId();
        Long feeAccountId = assetChangeProvider.getFeeAccountId();  // 平台收费账户ID
        Date nowDate = new Date();
        /* 还款金额 */
        long repayMoney = 0;
        for (RepayAssetChange repayAssetChange : repayAssetChanges) {
            repayMoney += repayAssetChange.getPrincipal() + repayAssetChange.getInterest();
            /* 回款记录 */
            BorrowCollection borrowCollection = repayAssetChange.getBorrowCollection();
            /* 投标记录 */
            Tender tender = tenderMap.get(borrowCollection.getTenderId());
            // 归还本金和利息
            BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            if (tender.getType().intValue() == 1) {
                batchAssetChangeItem.setType(AssetChangeTypeEnum.financeReceivedPayments.getLocalType());  // 名义借款人收到垫付还款
                batchAssetChangeItem.setAssetType(AssetTypeContants.finance);
            } else if (advance) {//判断是否是垫付
                batchAssetChangeItem.setType(AssetChangeTypeEnum.compensatoryReceivedPayments.getLocalType());  // 名义借款人收到垫付还款
            } else {
                batchAssetChangeItem.setType(AssetChangeTypeEnum.receivedPayments.getLocalType()); //借款人收到还款
            }

            batchAssetChangeItem.setUserId(repayAssetChange.getUserId());
            batchAssetChangeItem.setForUserId(repayUserId);  // 还款人
            batchAssetChangeItem.setMoney(repayAssetChange.getPrincipal() + repayAssetChange.getInterest());   // 本金加利息
            batchAssetChangeItem.setInterest(repayAssetChange.getInterest());  // 利息
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setSourceId(borrowCollection.getId());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItem.setRemark(String.format("收到客户对借款[%s]第%s期的还款", borrow.getName(), (borrowRepayment.getOrder() + 1)));
            batchAssetChangeItemService.save(batchAssetChangeItem);
            // 扣除利息管理费
            if (repayAssetChange.getInterestFee() > 0) {
                batchAssetChangeItem = new BatchAssetChangeItem();
                batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                batchAssetChangeItem.setState(0);
                batchAssetChangeItem.setType(AssetChangeTypeEnum.interestManagementFee.getLocalType());  // 扣除投资人利息管理费
                if (tender.getType().intValue() == 1) {
                    batchAssetChangeItem.setAssetType(AssetTypeContants.finance);
                }
                batchAssetChangeItem.setUserId(repayAssetChange.getUserId());
                batchAssetChangeItem.setForUserId(feeAccountId);
                batchAssetChangeItem.setMoney(repayAssetChange.getInterestFee());
                batchAssetChangeItem.setRemark(String.format("扣除借款标的[%s]利息管理费%s元", borrow.getName(), StringHelper.formatDouble(repayAssetChange.getInterestFee() / 100D, false)));
                batchAssetChangeItem.setCreatedAt(nowDate);
                batchAssetChangeItem.setUpdatedAt(nowDate);
                batchAssetChangeItem.setSourceId(borrowRepayment.getId());
                batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
                batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                batchAssetChangeItemService.save(batchAssetChangeItem);

                // 收费账户添加利息管理费用
                batchAssetChangeItem = new BatchAssetChangeItem();
                batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                batchAssetChangeItem.setState(0);
                batchAssetChangeItem.setType(AssetChangeTypeEnum.platformInterestManagementFee.getLocalType());  // 收费账户添加利息管理费用
                if (tender.getType().intValue() == 1) {
                    batchAssetChangeItem.setAssetType(AssetTypeContants.finance);
                }
                batchAssetChangeItem.setUserId(feeAccountId);
                batchAssetChangeItem.setForUserId(repayAssetChange.getUserId());
                batchAssetChangeItem.setMoney(repayAssetChange.getInterestFee());
                batchAssetChangeItem.setRemark(String.format("收取借款标的[%s]利息管理费%s元", borrow.getName(), StringHelper.formatDouble(repayAssetChange.getInterestFee() / 100D, false)));
                batchAssetChangeItem.setCreatedAt(nowDate);
                batchAssetChangeItem.setUpdatedAt(nowDate);
                batchAssetChangeItem.setSourceId(borrowRepayment.getId());
                batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
                batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                batchAssetChangeItemService.save(batchAssetChangeItem);
            }

            // 收取逾期管理费
            if (repayAssetChange.getOverdueFee() > 0) {
                batchAssetChangeItem = new BatchAssetChangeItem();
                batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                batchAssetChangeItem.setState(0);
                batchAssetChangeItem.setType(AssetChangeTypeEnum.platformRepayMentPenaltyFee.getLocalType());  // 收取逾期管理费
                if (tender.getType().intValue() == 1) {
                    batchAssetChangeItem.setAssetType(AssetTypeContants.finance);
                }
                batchAssetChangeItem.setUserId(feeAccountId);
                batchAssetChangeItem.setForUserId(repayAssetChange.getUserId());
                batchAssetChangeItem.setMoney(repayAssetChange.getPlatformOverdueFee());
                batchAssetChangeItem.setRemark(String.format("收取借款标的[%s]逾期管理费%s元", borrow.getName(), StringHelper.formatDouble(repayAssetChange.getPlatformOverdueFee() / 100D, false)));
                batchAssetChangeItem.setCreatedAt(nowDate);
                batchAssetChangeItem.setUpdatedAt(nowDate);
                batchAssetChangeItem.setSourceId(borrowRepayment.getId());
                batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
                batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                batchAssetChangeItemService.save(batchAssetChangeItem);
            }

            //扣除投资人待收
            if (tender.getType().intValue() != 1) {
                batchAssetChangeItem = new BatchAssetChangeItem();
                batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                batchAssetChangeItem.setState(0);
                batchAssetChangeItem.setType(AssetChangeTypeEnum.collectionSub.getLocalType());  //  扣除投资人待收
                if (tender.getType().intValue() == 1) {
                    batchAssetChangeItem.setAssetType(AssetTypeContants.finance);
                }
                batchAssetChangeItem.setUserId(repayAssetChange.getUserId());
                batchAssetChangeItem.setMoney(borrowCollection.getCollectionMoney());
                batchAssetChangeItem.setInterest(borrowCollection.getInterest());
                batchAssetChangeItem.setRemark(String.format("收到客户对[%s]借款的还款,扣除待收", borrow.getName()));
                batchAssetChangeItem.setCreatedAt(nowDate);
                batchAssetChangeItem.setUpdatedAt(nowDate);
                batchAssetChangeItem.setSourceId(borrowRepayment.getId());
                batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
                batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                batchAssetChangeItemService.save(batchAssetChangeItem);
            }
        }
        return repayMoney;
    }

    /**
     * 生成存管还款计划(递归调用解决转让问题)
     *
     * @param borrow
     * @param repayAccountId
     * @param repayAssetChanges
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Repay> calculateRepayPlan(Borrow borrow, String repayAccountId, List<Tender> tenderList,
                                          List<BorrowCollection> borrowCollectionList,
                                          double interestPercent, List<RepayAssetChange> repayAssetChanges) throws Exception {
        List<Repay> repayList = new ArrayList<>();
        Map<Long/* 投资记录*/, BorrowCollection/* 对应的还款计划*/> borrowCollectionMap = borrowCollectionList.stream().collect(Collectors.toMap(BorrowCollection::getTenderId, Function.identity()));
        /* 投标记录集合 */
        Map<Long, Tender> tenderMaps = tenderList.stream().collect(Collectors.toMap(Tender::getId, Function.identity()));
        /* 投资会员id集合 */
        Set<Long> userIds = tenderList.stream().map(p -> p.getUserId()).collect(Collectors.toSet());
        /* 投资人存管记录列表 */
        Specification<UserThirdAccount> uts = Specifications
                .<UserThirdAccount>and()
                .in("userId", userIds.toArray())
                .build();
        List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(uts);
        Preconditions.checkState(!CollectionUtils.isEmpty(userThirdAccountList), "生成即信还款计划: 查询用户存管开户记录列表为空!");
        Map<Long/* 用户ID*/, UserThirdAccount /* 用户存管*/> userThirdAccountMap = userThirdAccountList
                .stream()
                .collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));

        long sumCollectionInterest = borrowCollectionList.stream()
                .filter(borrowCollection -> tenderMaps.get(borrowCollection.getTenderId()).getTransferFlag() != 2)
                .mapToLong(BorrowCollection::getInterest).sum();  // 通过回款计划的利息获取回款总利息


        for (Tender tender : tenderList) {
            long inIn = 0; // 出借人的利息
            long inPr = 0; // 出借人的本金
            int inFee = 0; // 出借人利息费用
            int outFee = 0; // 借款人管理费
            BorrowCollection borrowCollection = borrowCollectionMap.get(tender.getId());  // 还款计划
            // 标的转让中时, 需要取消出让信息
            if (tender.getTransferFlag() == 1) {
                transferBiz.cancelTransferByTenderId(tender.getId());
            }
            // 已经转让的债权, 可以跳过还款
            if (tender.getTransferFlag() == 2 || ObjectUtils.isEmpty(borrowCollection)) {
                continue;
            }

            RepayAssetChange repayAssetChange = new RepayAssetChange();
            repayAssetChanges.add(repayAssetChange);
            double inInDouble = MoneyHelper.multiply(borrowCollection.getInterest(), interestPercent, 0);
            // 还款利息
            inIn = MoneyHelper.doubleToLong(inInDouble);
            // 还款本金
            inPr = borrowCollection.getPrincipal();
            repayAssetChange.setUserId(tender.getUserId());
            repayAssetChange.setInterest(inIn);
            repayAssetChange.setPrincipal(inPr);
            repayAssetChange.setBorrowCollection(borrowCollection);

            //借款类型集合
            ImmutableSet<Integer> borrowTypeSet = ImmutableSet.of(0, 4);
            //用户来源集合
            /*            ImmutableSet<Integer> userSourceSet = ImmutableSet.of(12);*/
            // 车贷标和渠道标利息管理费，风车理财不收
            // 满标复审在2017-11-1号之前收取利息管理费
            if (borrowTypeSet.contains(borrow.getType()) &&
                    borrow.getRecheckAt().getTime() < DateHelper.stringToDate("2017-11-01 00:00:00").getTime()) {
                ImmutableSet<Long> stockholder = ImmutableSet.of(2480L, 1753L, 1699L,
                        3966L, 1413L, 1857L,
                        183L, 2327L, 2432L,
                        2470L, 2552L, 2739L,
                        3939L, 893L, 608L,
                        1216L);
                boolean between = isBetween(new Date(), DateHelper.stringToDate("2015-12-25 00:00:00"),
                        DateHelper.stringToDate("2017-12-31 23:59:59"));
                if ((stockholder.contains(tender.getUserId())) && (between)) {
                    inFee += 0;
                } else {
                    /*Long userId = tender.getUserId();
                    Users user = userService.findById(userId);
                    if (!StringUtils.isEmpty(user.getWindmillId())) { // 风车理财用户不收管理费
                        log.info(String.format("风车理财：%s", user));
                        inFee += 0;
                    } else {*/
                    // 利息管理费
                    inFee += MoneyHelper.doubleToint(MoneyHelper.multiply(inIn, 0.1D, 0));  // 利息问题
/*                    }*/
                }
            }

            repayAssetChange.setInterestFee(inFee);  // 利息管理费
            long overdueFee = 0;


            /* 还款orderId */
            String orderId = JixinHelper.getOrderId(JixinHelper.REPAY_PREFIX);
            Repay repay = new Repay();
            repay.setAccountId(repayAccountId);
            repay.setOrderId(orderId);
            repay.setTxAmount(StringHelper.formatDouble(MoneyHelper.divide(inPr, 100, 2), false));
            repay.setIntAmount(StringHelper.formatDouble(MoneyHelper.divide(inIn, 100, 2), false));
            repay.setTxFeeIn(StringHelper.formatDouble(MoneyHelper.divide(inFee, 100, 2), false));
            repay.setTxFeeOut(StringHelper.formatDouble(MoneyHelper.divide(outFee, 100, 2), false));
            repay.setProductId(borrow.getProductId());
            repay.setAuthCode(tender.getAuthCode());
            UserThirdAccount userThirdAccount = userThirdAccountMap.get(tender.getUserId());
            Preconditions.checkNotNull(userThirdAccount, "投资人未开户!");
            repay.setForAccountId(userThirdAccount.getAccountId());
            repayList.add(repay);
            //改变回款状态
            borrowCollection.setTRepayOrderId(orderId);
            borrowCollection.setLateInterest(overdueFee);
            borrowCollection.setCollectionMoneyYes(inPr + inIn);
            borrowCollection.setUpdatedAt(new Date());
            borrowCollectionService.updateById(borrowCollection);
        }
        return repayList;
    }

    @RequestMapping("/pub/repair/repay")
    @Transactional
    public void repairRepay() throws Exception {
        UserThirdAccount repayUserThirdAccount = userThirdAccountService.findByUserId(22002L);
        Borrow borrow = borrowService.findById(169825L);
        BorrowRepayment borrowRepayment = borrowRepaymentService.findById(173943L);
        Date nowDate = new Date();

        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 4)
                .eq("borrowId", borrow.getId())
                .build();
        List<Tender> tenderList = tenderService.findList(specification);
        Preconditions.checkState(!CollectionUtils.isEmpty(tenderList), "投资记录不存在!");
        Map<Long/*tenderId*/, Tender> tenderMap = tenderList.stream().collect(Collectors.toMap(Tender::getId, Function.identity()));
        /* 投资id集合 */
        List<Long> tenderIds = tenderList.stream().map(p -> p.getId()).collect(Collectors.toList());
        /* 投资人回款记录 */
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("transferFlag", 0)
                .eq("order", borrowRepayment.getOrder())
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Preconditions.checkState(!CollectionUtils.isEmpty(borrowCollectionList), "生成即信还款计划: 获取回款计划列表为空!");
        /* 资金变动集合 */
        List<RepayAssetChange> repayAssetChanges = new ArrayList<>();
        List<Repay> repays = calculateRepayPlan(borrow,
                repayUserThirdAccount.getAccountId(),
                tenderList,
                borrowCollectionList,
                1,
                repayAssetChanges);

        //所有交易金额 交易金额指的是txAmount字段
        double txAmount = 0;
        //所有交易利息
        double intAmount = 0;
        //所有还款手续费
        double txFeeOut = 0;
        for (Repay repay : repays) {
            txAmount = MoneyHelper.add(txAmount, NumberHelper.toDouble(repay.getTxAmount()));
            intAmount = MoneyHelper.add(intAmount, NumberHelper.toDouble(repay.getIntAmount()));
            txFeeOut = MoneyHelper.add(txFeeOut, NumberHelper.toDouble(repay.getTxFeeOut()));
        }


        /* 批次号 */
        String batchNo = jixinHelper.getBatchNo();
        /* 资产记录分组流水号 */
        String groupSeqNo = assetChangeProvider.getGroupSeqNo();
        // 生成投资人还款资金变动记录
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setSourceId(borrowRepayment.getId());
        batchAssetChange.setState(0);
        batchAssetChange.setType(BatchAssetChangeContants.BATCH_REPAY);
        batchAssetChange.setCreatedAt(new Date());
        batchAssetChange.setUpdatedAt(new Date());
        batchAssetChange.setBatchNo(batchNo);
        batchAssetChangeService.save(batchAssetChange);
        // 生成回款人资金变动记录  返回值实际还款本金和利息  不包括手续费
        long repayMoney = doGenerateAssetChangeRecodeByRepay(borrow, tenderMap, borrowRepayment, borrowRepayment.getUserId(), repayAssetChanges, groupSeqNo, batchAssetChange, false);

        //批次还款操作
        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(batchNo);
        request.setTxAmount(StringHelper.formatDouble(txAmount, false));
        request.setRetNotifyURL(javaDomain + "/pub/repair/call");
        request.setNotifyURL(javaDomain + "/pub/repair/call");
        request.setAcqRes(GSON.toJson(new HashMap<>()));
        request.setSubPacks(GSON.toJson(repays));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(repays.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(response.getReceived()))) {
            BatchCancelReq batchCancelReq = new BatchCancelReq();
            batchCancelReq.setBatchNo(batchNo);
            batchCancelReq.setTxAmount(StringHelper.formatDouble(txAmount, false));
            batchCancelReq.setTxCounts(StringHelper.toString(repays.size()));
            batchCancelReq.setChannel(ChannelContant.HTML);
            BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
            if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
                throw new Exception("即信批次撤销失败!");
            }
            throw new Exception(response.getRetMsg());
        }

        //记录日志
        ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
        thirdBatchLog.setBatchNo(batchNo);
        thirdBatchLog.setCreateAt(nowDate);
        thirdBatchLog.setUpdateAt(nowDate);
        thirdBatchLog.setTxDate(request.getTxDate());
        thirdBatchLog.setTxTime(request.getTxTime());
        thirdBatchLog.setSeqNo(request.getSeqNo());
        thirdBatchLog.setSourceId(borrowRepayment.getId());
        thirdBatchLog.setType(ThirdBatchLogContants.BATCH_REPAY);
        thirdBatchLog.setRemark("即信批次还款.(修复)");
        thirdBatchLog.setAcqRes(GSON.toJson(new HashMap<>()));
        thirdBatchLogService.save(thirdBatchLog);

        //记录批次处理日志
        thirdBatchDealLogBiz.recordThirdBatchDealLog(thirdBatchLog.getBatchNo(), thirdBatchLog.getSourceId(),
                ThirdBatchDealLogContants.SEND_REQUEST, true, ThirdBatchLogContants.BATCH_REPAY, "");
    }


    @RequestMapping("/pub/repair/deal")
    @Transactional
    public void repairRepayDeal(@RequestParam("batchNo") Object batchNo) {
        Borrow borrow = borrowService.findById(169825L);
        BorrowRepayment borrowRepayment = borrowRepaymentService.findById(173943L);
        Specification<Tender> specification = Specifications
                .<Tender>and()
                .eq("status", 4)
                .eq("borrowId", borrow.getId())
                .build();
        List<Tender> tenderList = tenderService.findList(specification);
        Preconditions.checkState(!CollectionUtils.isEmpty(tenderList), "投资记录不存在!");
        Map<Long/*tenderId*/, Tender> tenderMap = tenderList.stream().collect(Collectors.toMap(Tender::getId, Function.identity()));
        /* 投资id集合 */
        List<Long> tenderIds = tenderList.stream().map(p -> p.getId()).collect(Collectors.toList());
        /* 投资人回款记录 */
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("transferFlag", 0)
                .eq("order", borrowRepayment.getOrder())
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Preconditions.checkState(!CollectionUtils.isEmpty(borrowCollectionList), "生成即信还款计划: 获取回款计划列表为空!");

        //2.处理资金还款人、收款人资金变动
        try {
            batchAssetChangeHelper.batchAssetChangeAndCollection(borrowRepayment.getId(), String.valueOf(batchNo), BatchAssetChangeContants.BATCH_REPAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //9.变更理财计划参数
        updateFinanceByReceivedRepay(tenderList, tenderMap, borrowCollectionList);

    }

    @Autowired
    private FinancePlanBuyerService financePlanBuyerService;
    @Autowired
    private FinancePlanService financePlanService;

    /**
     * 变更理财计划参数
     *
     * @param tenderList
     * @param tenderMap
     * @param borrowCollectionList
     */
    public void updateFinanceByReceivedRepay(List<Tender> tenderList, Map<Long, Tender> tenderMap, List<BorrowCollection> borrowCollectionList) {
        boolean flag = false;
        //理财计划购买ids
        Set<Long> financeBuyIds = tenderList.stream().filter(tender -> tender.getType().intValue() == 1).map(Tender::getFinanceBuyId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(financeBuyIds)) {
            return;
        }
        Specification<FinancePlanBuyer> fpps = Specifications
                .<FinancePlanBuyer>and()
                .in("id", financeBuyIds.toArray())
                .build();
        List<FinancePlanBuyer> financePlanBuyerList = financePlanBuyerService.findList(fpps);
        Map<Long/* buyId */, FinancePlanBuyer> financePlanBuyerMap = financePlanBuyerList.stream().collect(Collectors.toMap(FinancePlanBuyer::getId, Function.identity()));
        //理财计划购买记录ids
        Set<Long> financePlanIds = financePlanBuyerList.stream().map(FinancePlanBuyer::getPlanId).collect(Collectors.toSet());
        Specification<FinancePlan> fps = Specifications
                .<FinancePlan>and()
                .in("id", financePlanIds.toArray())
                .build();
        List<FinancePlan> financePlanList = financePlanService.findList(fps);
        Map<Long/* financePlanId */, FinancePlan> financePlanMap = financePlanList.stream().collect(Collectors.toMap(FinancePlan::getId, Function.identity()));
        for (BorrowCollection borrowCollection : borrowCollectionList) {
            Tender tender = tenderMap.get(borrowCollection.getTenderId());
            if (tender.getType().intValue() == 1) { //理财计划需要变更理财计划参数
                /*理财计划购买记录*/
                FinancePlanBuyer financePlanBuyer = financePlanBuyerMap.get(tender.getFinanceBuyId());
                /*理财计划记录*/
                FinancePlan financePlan = financePlanMap.get(financePlanBuyer.getPlanId());
                long principal = borrowCollection.getPrincipal();
                financePlanBuyer.setLeftMoney(financePlanBuyer.getLeftMoney() + principal);
                financePlanBuyer.setRightMoney(financePlanBuyer.getRightMoney() - principal);
                financePlan.setLeftMoney(financePlan.getLeftMoney() + principal);
                financePlan.setRightMoney(financePlan.getRightMoney() - principal);
                if (!flag) {
                    flag = true;
                }
            }
        }
        financePlanBuyerService.save(financePlanBuyerList);
        financePlanService.save(financePlanList);
    }

    @RequestMapping("/pub/repair/call")
    public ResponseEntity<String> repairCall() {
        return ResponseEntity.ok("success");
    }


    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/batch/deal")
    @Transactional
    public void batchDeal(@RequestParam("sourceId") Object sourceId, @RequestParam("batchNo") Object batchNo) {
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", StringHelper.toString(sourceId))
                .eq("batchNo", StringHelper.toString(batchNo))
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
        if (CollectionUtils.isEmpty(thirdBatchLogList)) {
            return;
        }
    }

    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/credit/invest/query")
    @Transactional
    public void creditInvestQuery(@RequestParam("accountId") Object accountId, @RequestParam("orgOrderId") Object orgOrderId) {
        CreditInvestQueryReq request = new CreditInvestQueryReq();
        request.setChannel(ChannelContant.HTML);
        request.setAccountId(String.valueOf(accountId));
        request.setOrgOrderId(String.valueOf(orgOrderId));
        request.setAcqRes("1");
        CreditInvestQueryResp response = jixinManager.send(JixinTxCodeEnum.CREDIT_INVEST_QUERY, request, CreditInvestQueryResp.class);
        log.info("=========================================================================================");
        log.info("即信用户债权查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(response));
    }

    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/test/call")
    @Transactional
    public ResponseEntity<String> batchDeal() {
        return ResponseEntity.ok("success");
    }

    @ApiOperation("资产查询")
    @RequestMapping("/pub/asset/find")
    @Transactional
    public void findAsset(@RequestParam("sourceId") Object sourceId, @RequestParam("startDate") Object startDate,
                          @RequestParam("endDate") Object endDate, @RequestParam("pageIndex") Object pageIndex,
                          @RequestParam("pageSize") Object pageSize) {
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId(String.valueOf(sourceId));
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        log.info("=========================================================================================");
        log.info("即信用户资产查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(balanceQueryResponse));

        AccountDetailsQueryRequest request = new AccountDetailsQueryRequest();
        request.setAccountId(String.valueOf(sourceId));
        request.setStartDate(String.valueOf(startDate));
        request.setEndDate(String.valueOf(endDate));
        request.setChannel(ChannelContant.HTML);
        request.setType("0"); // 转入
        //request.setTranType("7820"); // 线下转账的
        request.setPageSize(String.valueOf(pageIndex));
        request.setPageNum(String.valueOf(pageSize));
        AccountDetailsQueryResponse response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY, request, AccountDetailsQueryResponse.class);
        log.info("=========================================================================================");
        log.info("即信用户资产流水查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(response));
    }

    @ApiOperation("解除冻结")
    @RequestMapping("/pub/unfreeze")
    @Transactional
    public void unfreeze(@RequestParam("freezeMoney") Object freezeMoney
            , @RequestParam("accountId") Object accountId
            , @RequestParam("freezeOrderId") Object freezeOrderId
            , @RequestParam("userId") Object userId) {
        //解除存管资金冻结
        String orderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);
        BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
        balanceUnfreezeReq.setAccountId(String.valueOf(accountId));
        balanceUnfreezeReq.setTxAmount(String.valueOf(freezeMoney));
        balanceUnfreezeReq.setChannel(ChannelContant.HTML);
        balanceUnfreezeReq.setOrderId(orderId);
        balanceUnfreezeReq.setOrgOrderId(String.valueOf(freezeOrderId));
        BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
            log.error("===========================================================================");
            log.error("即信批次还款解除冻结资金失败：" + balanceUnfreezeResp.getRetMsg());
            log.error("===========================================================================");
        }
        log.info(GSON.toJson(balanceUnfreezeResp));
        //立即还款冻结
        AssetChange assetChange = new AssetChange();
        assetChange.setType(AssetChangeTypeEnum.unfreeze);  // 招标失败解除冻结资金
        assetChange.setUserId(NumberHelper.toLong(userId));
        assetChange.setMoney(new Double(NumberHelper.toDouble(freezeMoney) * 100).longValue());
        assetChange.setRemark("解除冻结资金");
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error("即信批次还款解除冻结可用资金异常:", e);
        }
    }


    @ApiOperation("冻结查询")
    @RequestMapping("/pub/freeze/find")
    @Transactional
    public void findFreeze(@RequestParam("accountId") Object accountId, @RequestParam("startDate") Object startDate,
                           @RequestParam("endDate") Object endDate) {
        FreezeDetailsQueryRequest freezeDetailsQueryRequest = new FreezeDetailsQueryRequest();
        freezeDetailsQueryRequest.setChannel(ChannelContant.HTML);
        freezeDetailsQueryRequest.setState("0");
        freezeDetailsQueryRequest.setStartDate(String.valueOf(startDate));
        freezeDetailsQueryRequest.setEndDate(String.valueOf(endDate));
        freezeDetailsQueryRequest.setPageNum("1");
        freezeDetailsQueryRequest.setPageSize("20");
        freezeDetailsQueryRequest.setAccountId(String.valueOf(accountId));
        FreezeDetailsQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.FREEZE_DETAILS_QUERY, freezeDetailsQueryRequest, FreezeDetailsQueryResponse.class);
        log.info("=========================================================================================");
        log.info("用户即信冻结查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(balanceQueryResponse));
    }

    @ApiOperation("用户债权列表查询")
    @RequestMapping("/pub/bid/find/all")
    @Transactional
    public void findBidList(@RequestParam("accountId") Object accountId, @RequestParam("startDate") Object startDate, @RequestParam("productId") Object productId) {
        CreditDetailsQueryRequest creditDetailsQueryRequest = new CreditDetailsQueryRequest();
        creditDetailsQueryRequest.setAccountId(String.valueOf(accountId));
        creditDetailsQueryRequest.setStartDate(String.valueOf(startDate));
        if (!ObjectUtils.isEmpty(productId)) {
            creditDetailsQueryRequest.setProductId(String.valueOf(productId));
        }
        creditDetailsQueryRequest.setEndDate(DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM));
        creditDetailsQueryRequest.setState("0");
        creditDetailsQueryRequest.setPageNum("1");
        creditDetailsQueryRequest.setPageSize("20");
        CreditDetailsQueryResponse creditDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.CREDIT_DETAILS_QUERY,
                creditDetailsQueryRequest,
                CreditDetailsQueryResponse.class);
        log.info("=========================================================================================");
        log.info("用户债权列表查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(creditDetailsQueryResponse));
    }

    @ApiOperation("用户债权列表查询")
    @RequestMapping("/pub/bid/seed/credit")
    @Transactional
    public void testCredit(@RequestParam("transferId") Object transferId) {
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TRANSFER);
        mqConfig.setTag(MqTagEnum.AGAIN_VERIFY_TRANSFER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(transferId), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        log.info(String.format("transferBizImpl testCredit send mq %s", GSON.toJson(body)));
        mqHelper.convertAndSend(mqConfig);
    }

    @ApiOperation("结束债权转让债权")
    @RequestMapping("/pub/bid/end/credit")
    @Transactional
    public void endTransfer(@RequestParam("transferId") Object transferId) {
        Transfer transfer = transferService.findById(NumberHelper.toLong(transferId));
        //推送队列结束债权转让第三方转让债权
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
        mqConfig.setTag(MqTagEnum.END_CREDIT_BY_TRANSFER);
        mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 1));
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(transfer.getBorrowId()), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("thirdBatchProvider endPcThirdTransferTender send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("thirdBatchProvider endPcThirdTransferTender send mq exception", e);
        }
    }

    @ApiOperation("用户债权列表查询")
    @RequestMapping("/pub/test/batch/cancel")
    @Transactional
    public void cancelBatch(@RequestParam("batchNo") Object batchNo, @RequestParam("txAmount") Object txAmount, @RequestParam("count") Object count) {
        BatchCancelReq batchCancelReq = new BatchCancelReq();
        batchCancelReq.setBatchNo(String.valueOf(batchNo));
        batchCancelReq.setTxAmount(String.valueOf(txAmount));
        batchCancelReq.setTxCounts(String.valueOf(count));
        batchCancelReq.setChannel(ChannelContant.HTML);
        BatchCancelResp batchCancelResp = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, batchCancelReq, BatchCancelResp.class);
        if ((ObjectUtils.isEmpty(batchCancelResp)) || (!ObjectUtils.isEmpty(batchCancelResp.getRetCode()))) {
            log.error("即信批次撤销失败!" + GSON.toJson(batchCancelResp));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @ApiOperation("批次查询")
    @RequestMapping("/pub/batch/find")
    @Transactional
    public void findBatch(@RequestParam("txDate") Object txDate, @RequestParam("batchNo") Object batchNo) {
        BatchQueryReq req = new BatchQueryReq();
        req.setChannel(ChannelContant.HTML);
        req.setBatchNo(StringHelper.toString(batchNo));
        req.setBatchTxDate(String.valueOf(txDate));
        BatchQueryResp resp = jixinManager.send(JixinTxCodeEnum.BATCH_QUERY, req, BatchQueryResp.class);
        log.info("=========================================================================================");
        log.info("即信批次状态查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(resp));

        BatchDetailsQueryReq batchDetailsQueryReq = new BatchDetailsQueryReq();
        batchDetailsQueryReq.setBatchNo(StringHelper.toString(batchNo));
        batchDetailsQueryReq.setBatchTxDate(String.valueOf(txDate));
        batchDetailsQueryReq.setType("0");
        batchDetailsQueryReq.setPageNum("1");
        batchDetailsQueryReq.setPageSize("20");
        batchDetailsQueryReq.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, batchDetailsQueryReq, BatchDetailsQueryResp.class);
        log.info("=========================================================================================");
        log.info("即信批次状态详情查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(batchDetailsQueryResp));
    }

    @ApiOperation("投标申请查询")
    @RequestMapping("/pub/bid/find")
    @Transactional
    public void bidApplyQuery(@RequestParam("orderId") Object orderId, @RequestParam("accountId") Object accountId) {
        BidApplyQueryRequest request = new BidApplyQueryRequest();
        request.setAccountId(String.valueOf(accountId));
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId(String.valueOf(orderId));
        BidApplyQueryResponse response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResponse.class);
        log.info("=========================================================================================");
        log.info("即信批次状态详情查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(response));
    }


    @Autowired
    BorrowBiz borrowBiz;

    @Autowired
    TenderService tenderService;

    @GetMapping("pub/test/marketing")
    public void touchMarketing() {
        Tender tender = tenderService.findById(262363L);
        borrowBiz.touchMarketingByTender(tender);

    }

}
