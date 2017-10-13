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
import com.gofobao.framework.common.integral.IntegralChangeEntity;
import com.gofobao.framework.common.integral.IntegralChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BatchAssetChangeHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
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
    @RequestMapping("/pub/test/call")
    @Transactional
    public ResponseEntity<String> batchDeal() {
        return ResponseEntity.ok("success");
    }

    @Autowired
    private TransferBuyLogService transferBuyLogService;

    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/test/transfer/repair")
    @Transactional
    public void repairTransfer(@RequestParam("transferId") Object transferId) {
        /* 债权转让记录 */
        Transfer transfer = transferService.findById(NumberHelper.toLong(transferId));
        Tender patentTender = tenderService.findById(transfer.getTenderId());
        Borrow parentBorrow = borrowService.findById(transfer.getBorrowId());
        Specification<TransferBuyLog> tbls = Specifications
                .<TransferBuyLog>and()
                .eq("transferId", transfer.getId())
                .eq("state", 1)
                .build();
        List<TransferBuyLog> transferBuyLogList = transferBuyLogService.findList(tbls);
        List<Tender> childTenderList = addFinanceChildTender(transfer.getRecheckAt(), transfer, patentTender, transferBuyLogList);
        try {
            addFinanceChildTenderCollection(transfer.getRecheckAt(), transfer, parentBorrow, childTenderList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成子级债权回款记录，标注老债权回款已经转出
     *
     * @param nowDate
     * @param transfer
     * @param parentBorrow
     * @param childTenderList
     */
    public List<BorrowCollection> addFinanceChildTenderCollection(Date nowDate, Transfer transfer, Borrow parentBorrow, List<Tender> childTenderList) throws Exception {
        List<BorrowCollection> childTenderCollectionList = new ArrayList<>();/* 债权子记录回款记录 */
        String borrowCollectionIds = transfer.getBorrowCollectionIds();
        //生成子级债权回款记录，标注老债权回款已经转出
        Specification<BorrowCollection> bcs = null;
        if (transfer.getIsAll()) {
            bcs = Specifications
                    .<BorrowCollection>and()
                    .eq("tenderId", transfer.getTenderId())
                    .eq("status", 0)
                    .build();
        } else {
            bcs = Specifications
                    .<BorrowCollection>and()
                    .eq("tenderId", transfer.getTenderId())
                    .eq("id", borrowCollectionIds.split(","))
                    .eq("status", 0)
                    .build();
        }
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);/* 债权转让原投资回款记录 */
        long transferInterest = borrowCollectionList.stream().mapToLong(BorrowCollection::getInterest).sum();/* 债权转让总利息 */
        Date repayAt = transfer.getRepayAt();/* 原借款下一期还款日期 */
        Date startAt = null;/* 计息开始时间 */
        if (parentBorrow.getRepayFashion() == 1) {
            startAt = DateHelper.subDays(repayAt, parentBorrow.getTimeLimit());
        } else if (parentBorrow.getRepayFashion() == 0 || parentBorrow.getRepayFashion() == 2) {
            startAt = DateHelper.subMonths(repayAt, 1);
        }

        for (int j = 0; j < childTenderList.size(); j++) {
            Tender childTender = childTenderList.get(j);/* 购买债权转让子投资记录 */
            //生成购买债权转让新的回款记录
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                    childTender.getValidMoney().doubleValue(),
                    transfer.getApr().doubleValue(),
                    transfer.getTimeLimit(),
                    startAt);
            Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(parentBorrow.getRepayFashion());
            List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");
            Preconditions.checkState(!CollectionUtils.isEmpty(repayDetailList), "生成用户回款计划开始: 计划生成为空");
            BorrowCollection borrowCollection;
            int startOrder = borrowCollectionList.get(0).getOrder();/* 获取开始转让期数,期数下标从0开始 */
            long sumCollectionInterest = 0l;/*总回款利息*/
            for (int i = 0; i < repayDetailList.size(); i++) {
                borrowCollection = new BorrowCollection();
                Map<String, Object> repayDetailMap = repayDetailList.get(i);
                long principal = NumberHelper.toLong(repayDetailMap.get("principal"));
                long interest = NumberHelper.toLong(repayDetailMap.get("interest"));
                Date collectionAt = DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt")));
                sumCollectionInterest += interest;
                //最后一个购买债权转让的最后一期回款，需要把还款溢出的利息补给新的回款记录
                if ((j == childTenderList.size() - 1) && (i == repayDetailList.size() - 1)) {
                    interest += transferInterest - sumCollectionInterest;/* 新的回款利息添加溢出的利息 */
                }

                //如果是理财计划借款不需要生成利息
                if (childTender.getType().intValue() == 1) {
                    interest = 0;
                }

                borrowCollection.setTenderId(childTender.getId());
                borrowCollection.setStatus(0);
                borrowCollection.setOrder(startOrder++);
                borrowCollection.setUserId(childTender.getUserId());
                borrowCollection.setStartAt(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : startAt);
                borrowCollection.setStartAtYes(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : nowDate);
                borrowCollection.setCollectionAt(collectionAt);
                borrowCollection.setCollectionAtYes(collectionAt);
                borrowCollection.setCollectionMoney(principal);
                borrowCollection.setPrincipal(principal);
                borrowCollection.setInterest(interest);
                borrowCollection.setCreatedAt(nowDate);
                borrowCollection.setUpdatedAt(nowDate);
                borrowCollection.setCollectionMoneyYes(0l);
                borrowCollection.setLateDays(0);
                borrowCollection.setLateInterest(0l);
                borrowCollection.setBorrowId(parentBorrow.getId());
                childTenderCollectionList.add(borrowCollection);

            }
            borrowCollectionService.save(childTenderCollectionList);
        }

        return childTenderCollectionList;
    }

    /**
     * 新增理财计划子级标的
     *
     * @param nowDate
     * @param transfer
     * @param parentTender
     * @param transferBuyLogList
     * @return
     */
    public List<Tender> addFinanceChildTender(Date nowDate, Transfer transfer, Tender parentTender, List<TransferBuyLog> transferBuyLogList) {
        //生成债权记录与回款记录
        List<Tender> childTenderList = new ArrayList<>();
        transferBuyLogList.stream().forEach(transferBuyLog -> {
            Tender childTender = new Tender();
            UserThirdAccount buyUserThirdAccount = userThirdAccountService.findByUserId(transferBuyLog.getUserId());

            childTender.setUserId(transferBuyLog.getUserId());
            childTender.setStatus(1);
            childTender.setType(transferBuyLog.getType());
            childTender.setBorrowId(transfer.getBorrowId());
            childTender.setSource(transferBuyLog.getSource());
            childTender.setIsAuto(transferBuyLog.getAuto());
            childTender.setAutoOrder(transferBuyLog.getAutoOrder());
            childTender.setMoney(transferBuyLog.getBuyMoney());
            childTender.setValidMoney(transferBuyLog.getPrincipal());
            childTender.setTransferFlag(0);
            childTender.setTUserId(buyUserThirdAccount.getUserId());
            childTender.setParentId(parentTender.getId());
            childTender.setState(2);
            childTender.setTransferBuyId(transferBuyLog.getId());
            childTender.setAlreadyInterest(transferBuyLog.getAlreadyInterest());
            childTender.setThirdTenderOrderId(transferBuyLog.getThirdTransferOrderId());
            childTender.setAuthCode(transferBuyLog.getTransferAuthCode());
            childTender.setCreatedAt(nowDate);
            childTender.setUpdatedAt(nowDate);
            childTenderList.add(childTender);

            //更新购买净值标状态为成功购买
            transferBuyLog.setState(1);
            transferBuyLog.setUpdatedAt(new Date());
        });
        transferBuyLogService.save(transferBuyLogList);
        //保存生成投标记录
        tenderService.save(childTenderList);

        transfer.setTenderCount(transferBuyLogList.size());
        return childTenderList;
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
