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
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryRequest;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryResponse;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
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
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
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
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
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
    public List<Repay> calculateRepayPlan(Borrow borrow, String repayAccountId,
                                          BorrowCollection borrowCollection,
                                          double interestPercent, List<RepayAssetChange> repayAssetChanges) throws Exception {
        List<Repay> repayList = new ArrayList<>();
        /* 投资人存管记录列表 */
        Specification<UserThirdAccount> uts = Specifications
                .<UserThirdAccount>and()
                .in("userId", borrowCollection.getUserId())
                .build();
        List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findList(uts);
        Preconditions.checkNotNull(userThirdAccountList, "生成即信还款计划: 查询用户存管开户记录列表为空!");
        Map<Long/* 用户ID*/, UserThirdAccount /* 用户存管*/> userThirdAccountMap = userThirdAccountList
                .stream()
                .collect(Collectors.toMap(UserThirdAccount::getUserId, Function.identity()));

        long inIn = 0; // 出借人的利息
        long inPr = 0; // 出借人的本金
        int inFee = 0; // 出借人利息费用
        int outFee = 0; // 借款人管理费


        RepayAssetChange repayAssetChange = new RepayAssetChange();
        repayAssetChanges.add(repayAssetChange);
        double inInDouble = MoneyHelper.multiply(borrowCollection.getInterest(), interestPercent, 0);
        inIn = MoneyHelper.doubleToLong(inInDouble);  // 还款利息

        inPr = borrowCollection.getPrincipal(); // 还款本金
        repayAssetChange.setUserId(borrowCollection.getUserId());
        repayAssetChange.setInterest(inIn);
        repayAssetChange.setPrincipal(inPr);
        repayAssetChange.setBorrowCollection(borrowCollection);

        //借款类型集合
        ImmutableSet<Integer> borrowTypeSet = ImmutableSet.of(0, 4);
        //用户来源集合
            /*            ImmutableSet<Integer> userSourceSet = ImmutableSet.of(12);*/
        // 车贷标和渠道标利息管理费，风车理财不收
        if (borrowTypeSet.contains(borrow.getType())) {
            ImmutableSet<Long> stockholder = ImmutableSet.of(2480L, 1753L, 1699L,
                    3966L, 1413L, 1857L,
                    183L, 2327L, 2432L,
                    2470L, 2552L, 2739L,
                    3939L, 893L, 608L,
                    1216L);
            boolean between = isBetween(new Date(), DateHelper.stringToDate("2015-12-25 00:00:00"),
                    DateHelper.stringToDate("2017-12-31 23:59:59"));
            if ((stockholder.contains(borrowCollection.getUserId())) && (between)) {
                inFee += 0;
            } else {
                Long userId = borrowCollection.getUserId();
                Users user = userService.findById(userId);
                if (!StringUtils.isEmpty(user.getWindmillId())) { // 风车理财用户不收管理费
                    log.info(String.format("风车理财：%s", user));
                    inFee += 0;
                } else {
                    // 利息管理费
                    inFee += MoneyHelper.doubleToint(MoneyHelper.multiply(inIn, 0.1D, 0));  // 利息问题
                }
            }
        }

        repayAssetChange.setInterestFee(inFee);  // 利息管理费


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
        repay.setAuthCode("20170905164818878751");
        UserThirdAccount userThirdAccount = userThirdAccountMap.get(borrowCollection.getUserId());
        Preconditions.checkNotNull(userThirdAccount, "投资人未开户!");
        repay.setForAccountId(userThirdAccount.getAccountId());
        repayList.add(repay);
        //改变回款状态
        borrowCollection.setTRepayOrderId(orderId);
        borrowCollection.setLateInterest(0l);
        borrowCollection.setCollectionMoneyYes(inPr + inIn);
        borrowCollection.setUpdatedAt(new Date());
        borrowCollectionService.updateById(borrowCollection);
        return repayList;
    }

    /**
     * 生成回款记录
     *
     * @param borrow
     * @param repayUserId
     * @param repayAssetChanges
     * @param batchAssetChange
     */
    private long doGenerateAssetChangeRecodeByRepay(Borrow borrow, long repayUserId, List<RepayAssetChange> repayAssetChanges, String groupSeqNo, BatchAssetChange batchAssetChange) throws ExecutionException {
        long batchAssetChangeId = batchAssetChange.getId();
        Long feeAccountId = assetChangeProvider.getFeeAccountId();  // 平台收费账户ID
        Date nowDate = new Date();
        /* 还款金额 */
        long repayMoney = 0;
        for (RepayAssetChange repayAssetChange : repayAssetChanges) {
            repayMoney += repayAssetChange.getPrincipal() + repayAssetChange.getInterest();
            /* 回款记录 */
            BorrowCollection borrowCollection = repayAssetChange.getBorrowCollection();
            // 归还本金和利息
            BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setType(AssetChangeTypeEnum.receivedPayments.getLocalType()); //借款人收到还款
            batchAssetChangeItem.setUserId(repayAssetChange.getUserId());
            batchAssetChangeItem.setForUserId(repayUserId);  // 还款人
            batchAssetChangeItem.setMoney(repayAssetChange.getPrincipal() + repayAssetChange.getInterest());   // 本金加利息
            batchAssetChangeItem.setInterest(repayAssetChange.getInterest());  // 利息
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setSourceId(borrowCollection.getId());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItem.setRemark(String.format("收到客户对借款[%s]第%s期的还款", borrow.getName(), (borrowCollection.getOrder() + 1)));
            batchAssetChangeItemService.save(batchAssetChangeItem);
            // 扣除利息管理费
            if (repayAssetChange.getInterestFee() > 0) {
                batchAssetChangeItem = new BatchAssetChangeItem();
                batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                batchAssetChangeItem.setState(0);
                batchAssetChangeItem.setType(AssetChangeTypeEnum.interestManagementFee.getLocalType());  // 扣除投资人利息管理费
                batchAssetChangeItem.setUserId(repayAssetChange.getUserId());
                batchAssetChangeItem.setForUserId(feeAccountId);
                batchAssetChangeItem.setMoney(repayAssetChange.getInterestFee());
                batchAssetChangeItem.setRemark(String.format("扣除借款标的[%s]利息管理费%s元", borrow.getName(), StringHelper.formatDouble(repayAssetChange.getInterestFee() / 100D, false)));
                batchAssetChangeItem.setCreatedAt(nowDate);
                batchAssetChangeItem.setUpdatedAt(nowDate);
                batchAssetChangeItem.setSourceId(borrowCollection.getId());
                batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
                batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                batchAssetChangeItemService.save(batchAssetChangeItem);

                // 收费账户添加利息管理费用
                batchAssetChangeItem = new BatchAssetChangeItem();
                batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
                batchAssetChangeItem.setState(0);
                batchAssetChangeItem.setType(AssetChangeTypeEnum.platformInterestManagementFee.getLocalType());  // 收费账户添加利息管理费用
                batchAssetChangeItem.setUserId(feeAccountId);
                batchAssetChangeItem.setForUserId(repayAssetChange.getUserId());
                batchAssetChangeItem.setMoney(repayAssetChange.getInterestFee());
                batchAssetChangeItem.setRemark(String.format("收取借款标的[%s]利息管理费%s元", borrow.getName(), StringHelper.formatDouble(repayAssetChange.getInterestFee() / 100D, false)));
                batchAssetChangeItem.setCreatedAt(nowDate);
                batchAssetChangeItem.setUpdatedAt(nowDate);
                batchAssetChangeItem.setSourceId(borrowCollection.getId());
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
                batchAssetChangeItem.setUserId(feeAccountId);
                batchAssetChangeItem.setForUserId(repayAssetChange.getUserId());
                batchAssetChangeItem.setMoney(repayAssetChange.getPlatformOverdueFee());
                batchAssetChangeItem.setRemark(String.format("收取借款标的[%s]逾期管理费%s元", borrow.getName(), StringHelper.formatDouble(repayAssetChange.getPlatformOverdueFee() / 100D, false)));
                batchAssetChangeItem.setCreatedAt(nowDate);
                batchAssetChangeItem.setUpdatedAt(nowDate);
                batchAssetChangeItem.setSourceId(borrowCollection.getId());
                batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
                batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
                batchAssetChangeItemService.save(batchAssetChangeItem);
            }

            //扣除投资人待收
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setType(AssetChangeTypeEnum.collectionSub.getLocalType());  //  扣除投资人待收
            batchAssetChangeItem.setUserId(repayAssetChange.getUserId());
            batchAssetChangeItem.setMoney(borrowCollection.getCollectionMoney());
            batchAssetChangeItem.setInterest(borrowCollection.getInterest());
            batchAssetChangeItem.setRemark(String.format("收到客户对[%s]借款的还款,扣除待收", borrow.getName()));
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSourceId(borrowCollection.getId());
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItemService.save(batchAssetChangeItem);
        }
        return repayMoney;
    }


    @ApiOperation("资产查询")
    @RequestMapping("/pub/test/call")
    @Transactional
    public ResponseEntity<String> repayResponse() {
        return ResponseEntity.ok("success");
    }

    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    /**
     * 正常还款流程
     *
     * @return
     * @throws Exception
     */
    @ApiOperation("资产查询")
    @RequestMapping("/pub/test/repay/repair")
    @Transactional
    public ResponseEntity<VoBaseResp> normalRepay() throws Exception {
        Date nowDate = new Date();
        String batchNo = jixinHelper.getBatchNo();    // 批次号
        String groupSeqNo = assetChangeProvider.getGroupSeqNo(); // 资产记录分组流水号

        String freezeOrderId = JixinHelper.getOrderId(JixinHelper.BALANCE_FREEZE_PREFIX);

        /* 投资人回款记录 */
        BorrowCollection borrowCollection = borrowCollectionService.findById(407061);

        Borrow borrow = borrowService.findById(borrowCollection.getBorrowId());

        UserThirdAccount repayUserThirdAccount = userThirdAccountService.findByUserId(22002l);
        /* 资金变动集合 */
        List<RepayAssetChange> repayAssetChanges = new ArrayList<>();
        List<Repay> repays = calculateRepayPlan(borrow,
                repayUserThirdAccount.getAccountId(),
                borrowCollection,
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
        double freezeMoney = MoneyHelper.round(MoneyHelper.add(MoneyHelper.add(txAmount, intAmount), txFeeOut), 2);
        // MoneyHelper.add(MoneyHelper.add(txAmount, intAmount),  txFeeOut, 2);
        // 生成投资人还款资金变动记录
        BatchAssetChange batchAssetChange = new BatchAssetChange();
        batchAssetChange.setSourceId(borrowCollection.getId());
        batchAssetChange.setState(0);
        batchAssetChange.setType(BatchAssetChangeContants.BATCH_REPAY);
        batchAssetChange.setCreatedAt(new Date());
        batchAssetChange.setUpdatedAt(new Date());
        batchAssetChange.setBatchNo(batchNo);
        batchAssetChangeService.save(batchAssetChange);
        // 生成回款人资金变动记录  返回值实际还款本金和利息  不包括手续费
        long repayMoney = doGenerateAssetChangeRecodeByRepay(borrow, borrowCollection.getUserId(), repayAssetChanges, groupSeqNo, batchAssetChange);
        //真实的逾期费用
        /*平台实际收取的逾期费用*/
        double realPlatformOverdueFee = 0;
        /*投资人实际收取的逾期费用*/
        double realOverdueFee = 0;
        for (RepayAssetChange repayAssetChange : repayAssetChanges) {
            realPlatformOverdueFee = MoneyHelper.add(realPlatformOverdueFee, repayAssetChange.getPlatformOverdueFee());
            realOverdueFee = MoneyHelper.add(realOverdueFee, repayAssetChange.getOverdueFee());
        }
        // 生成还款人还款批次资金改变记录
        addBatchAssetChangeByBorrower(batchAssetChange.getId(), borrowCollection, borrow,
                repayUserThirdAccount.getUserId(), groupSeqNo,
                repayMoney);

        // 冻结还款金额
        long money = new Double(MoneyHelper.round(MoneyHelper.multiply(freezeMoney, 100d), 0)).longValue();

        BalanceFreezeReq balanceFreezeReq = new BalanceFreezeReq();
        balanceFreezeReq.setAccountId(repayUserThirdAccount.getAccountId());
        balanceFreezeReq.setTxAmount(StringHelper.formatDouble(freezeMoney, false));
        balanceFreezeReq.setOrderId(freezeOrderId);
        balanceFreezeReq.setChannel(ChannelContant.HTML);
        BalanceFreezeResp balanceFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_FREEZE, balanceFreezeReq, BalanceFreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceFreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceFreezeResp.getRetCode()))) {
            throw new Exception(String.format("正常还款流程：%s,userId:%s,collectionId:%s,borrowId:%s", balanceFreezeResp.getRetMsg(), repayUserThirdAccount.getUserId(), borrowCollection.getId(), borrow.getId()));
        }

        try {
            AssetChange freezeAssetChange = new AssetChange();
            freezeAssetChange.setForUserId(repayUserThirdAccount.getUserId());
            freezeAssetChange.setUserId(repayUserThirdAccount.getUserId());
            freezeAssetChange.setType(AssetChangeTypeEnum.freeze);
            freezeAssetChange.setRemark(String.format("成功还款标的[%s]冻结", borrow.getName(), StringHelper.formatDouble(money, 100D, true)));
            freezeAssetChange.setSeqNo(assetChangeProvider.getSeqNo());
            freezeAssetChange.setMoney(money);
            freezeAssetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            freezeAssetChange.setSourceId(borrowCollection.getId());
            assetChangeProvider.commonAssetChange(freezeAssetChange);

            Map<String, Object> acqResMap = new HashMap<>();
            acqResMap.put("userId", repayUserThirdAccount.getUserId());
            acqResMap.put("collectionId", borrowCollection.getId());
            acqResMap.put("freezeOrderId", freezeOrderId);

            //批量放款
            acqResMap.put("freezeMoney", freezeMoney);

            //批次还款操作
            BatchRepayReq request = new BatchRepayReq();
            request.setBatchNo(batchNo);
            request.setTxAmount(StringHelper.formatDouble(txAmount, false));
            request.setRetNotifyURL(javaDomain + "/pub/test/call");
            request.setNotifyURL(javaDomain + "/pub/test/call");
            request.setAcqRes(GSON.toJson(acqResMap));
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
            thirdBatchLog.setSourceId(borrowCollection.getId());
            thirdBatchLog.setType(ThirdBatchLogContants.BATCH_REPAY);
            thirdBatchLog.setRemark("即信批次还款.(债权转让bug)");
            thirdBatchLog.setAcqRes(GSON.toJson(acqResMap));
            thirdBatchLogService.save(thirdBatchLog);

            //记录批次处理日志
            thirdBatchDealLogBiz.recordThirdBatchDealLog(thirdBatchLog.getBatchNo(), thirdBatchLog.getSourceId(),
                    ThirdBatchDealLogContants.SEND_REQUEST, true, ThirdBatchLogContants.BATCH_REPAY, "");
        } catch (Exception e) {

            // 申请即信还款解冻
            String unfreezeOrderId = JixinHelper.getOrderId(JixinHelper.BALANCE_UNFREEZE_PREFIX);
            BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
            balanceUnfreezeReq.setAccountId(repayUserThirdAccount.getAccountId());
            balanceUnfreezeReq.setTxAmount(StringHelper.formatDouble(freezeMoney, false));
            balanceUnfreezeReq.setOrderId(unfreezeOrderId);
            balanceUnfreezeReq.setOrgOrderId(freezeOrderId);
            balanceUnfreezeReq.setChannel(ChannelContant.HTML);
            BalanceUnfreezeResp balanceUnFreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
            if ((ObjectUtils.isEmpty(balanceUnfreezeReq)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnFreezeResp.getRetCode()))) {
                throw new Exception("正常还款解冻资金异常：" + balanceUnFreezeResp.getRetMsg());
            }
            throw new Exception(e);
        }
        return ResponseEntity.ok(VoBaseResp.ok("还款正常"));
    }

    /**
     * 生成还款人还款批次资金改变记录
     *
     * @param batchAssetChangeId
     * @param borrow
     * @param groupSeqNo
     * @param actualMoney        真是金额
     */
    public void addBatchAssetChangeByBorrower(long batchAssetChangeId,
                                              BorrowCollection borrowCollection,
                                              Borrow borrow,
                                              long repayUserId,
                                              String groupSeqNo,
                                              long actualMoney) {
        Date nowDate = new Date();
        // 借款人还款
        BatchAssetChangeItem batchAssetChangeItem = new BatchAssetChangeItem();
        batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
        batchAssetChangeItem.setState(0);
        batchAssetChangeItem.setType(AssetChangeTypeEnum.repayment.getLocalType());  // 还款
        batchAssetChangeItem.setUserId(repayUserId);
        batchAssetChangeItem.setMoney(actualMoney);
        batchAssetChangeItem.setRemark(String.format("对借款[%s]第%s期的还款（债权转让bug，补）",
                borrow.getName(),
                StringHelper.toString(borrowCollection.getOrder() + 1)));

        batchAssetChangeItem.setCreatedAt(nowDate);
        batchAssetChangeItem.setUpdatedAt(nowDate);
        batchAssetChangeItem.setSourceId(borrowCollection.getId());
        batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
        batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
        batchAssetChangeItemService.save(batchAssetChangeItem);

        if (ObjectUtils.isEmpty(borrow.getTakeUserId()) && borrow.getUserId().intValue() == repayUserId) { //当借款不是受托支付，并且是本人还款才会进行待还扣减
            // 扣除借款人待还
            batchAssetChangeItem = new BatchAssetChangeItem();
            batchAssetChangeItem.setBatchAssetChangeId(batchAssetChangeId);
            batchAssetChangeItem.setState(0);
            batchAssetChangeItem.setType(AssetChangeTypeEnum.paymentSub.getLocalType());  // 扣除待还
            batchAssetChangeItem.setUserId(repayUserId);
            batchAssetChangeItem.setMoney(borrowCollection.getPrincipal() + borrowCollection.getInterest());
            batchAssetChangeItem.setInterest(borrowCollection.getInterest());
            batchAssetChangeItem.setRemark("还款成功扣除待还（债权转让bug，补）");
            batchAssetChangeItem.setCreatedAt(nowDate);
            batchAssetChangeItem.setUpdatedAt(nowDate);
            batchAssetChangeItem.setSourceId(borrowCollection.getId());
            batchAssetChangeItem.setSeqNo(assetChangeProvider.getSeqNo());
            batchAssetChangeItem.setGroupSeqNo(groupSeqNo);
            batchAssetChangeItemService.save(batchAssetChangeItem);
        }
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
        if (ObjectUtils.isEmpty(productId)) {
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
