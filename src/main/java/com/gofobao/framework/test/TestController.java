package com.gofobao.framework.test;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.balance_query.BalanceQueryRequest;
import com.gofobao.framework.api.model.balance_query.BalanceQueryResponse;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeReq;
import com.gofobao.framework.api.model.balance_un_freeze.BalanceUnfreezeResp;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.batch_query.BatchQueryReq;
import com.gofobao.framework.api.model.batch_query.BatchQueryResp;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryRequest;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResponse;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryRequest;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryResponse;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryRequest;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryResponse;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.entity.NewAssetLog;
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
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/borrow/collection/send")
    @Transactional(rollbackFor = Exception.class)
    public void sendBorrowCollection() {
        long redpackAccountId = 0;
        try {
            redpackAccountId = assetChangeProvider.getRedpackAccountId();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redpackAccountId);
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(22002l);

        String collectionIds = "418565,418713,418716,419532,419541,420643,420848,420854,420966,420975,421002,421011,421020,421118,421125,422198,423838";
        String[] idArr = collectionIds.split(",");
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("id", idArr)
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Set<Long> borrowIds = borrowCollectionList.stream().map(BorrowCollection::getBorrowId).collect(Collectors.toSet());
        Specification<Borrow> bs = Specifications
                .<Borrow>and()
                .in("id", borrowIds.toArray())
                .build();
        List<Borrow> borrowList = borrowService.findList(bs);
        Map<Long, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        for (BorrowCollection borrowCollection : borrowCollectionList) {
            Specification<NewAssetLog> nals = Specifications
                    .<NewAssetLog>and()
                    .eq("userId", borrowCollection.getUserId())
                    .eq("localType", AssetChangeTypeEnum.makeUpReceivedPayments.getLocalType())
                    .build();
            long count = newAssetLogService.count(nals);
            if (count > 0) {
                continue;
            }

            Borrow borrow = borrowMap.get(borrowCollection.getBorrowId());
            long principal = borrowCollection.getPrincipal();
            long interest = borrowCollection.getInterest();
            long interestFee = 0;

            ImmutableSet<Long> stockholder = ImmutableSet.of(2480L, 1753L, 1699L,
                    3966L, 1413L, 1857L,
                    183L, 2327L, 2432L,
                    2470L, 2552L, 2739L,
                    3939L, 893L, 608L,
                    1216L);
            boolean between = isBetween(new Date(), DateHelper.stringToDate("2015-12-25 00:00:00"),
                    DateHelper.stringToDate("2017-12-31 23:59:59"));

            if ((stockholder.contains(borrowCollection.getUserId())) && (between)) {
            } else {
                Long feeAccountId = null;  // 平台收费账户ID
                try {
                    feeAccountId = assetChangeProvider.getFeeAccountId();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                interestFee = MoneyHelper.doubleToint(MoneyHelper.multiply(interest, 0.1D, 0));  // 利息问题

                AssetChange assetChange = new AssetChange();   // 还款本金和利息
                assetChange = new AssetChange();
                assetChange.setType(AssetChangeTypeEnum.interestManagementFee);  // 扣除投资人利息管理费
                assetChange.setUserId(borrowCollection.getUserId());
                assetChange.setForUserId(feeAccountId);
                assetChange.setMoney(interestFee);
                assetChange.setRemark(String.format("扣除借款标的[%s]利息管理费%s元(补收)", borrow.getName(), StringHelper.formatDouble(interestFee / 100D, false)));
                assetChange.setSeqNo(assetChangeProvider.getSeqNo());
                assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
                try {
                    assetChangeProvider.commonAssetChange(assetChange);
                } catch (Exception e) {
                    log.error("补发失败:", e);
                }
            }

            AssetChange assetChange = new AssetChange();   // 还款本金和利息
            assetChange.setType(AssetChangeTypeEnum.makeUpReceivedPayments);  // 投资人收到还款
            assetChange.setUserId(borrowCollection.getUserId());
            assetChange.setForUserId(0l);
            assetChange.setMoney(principal + interest);   // 本金加利息
            assetChange.setInterest(interest);  // 利息
            assetChange.setRemark(String.format("收到客户对借款[%s]第%s期的还款(补发)", borrow.getName(), (borrowCollection.getOrder() + 1)));
            assetChange.setSourceId(borrowCollection.getId());
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error("补发失败:", e);
            }


            //扣除投资人待收
            assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.collectionSub);  //  扣除投资人待收
            assetChange.setUserId(borrowCollection.getUserId());
            assetChange.setMoney(borrowCollection.getInterest() + borrowCollection.getPrincipal());
            assetChange.setInterest(borrowCollection.getInterest());
            assetChange.setRemark(String.format("收到客户对[%s]借款的还款,扣除待收(补发)", borrow.getName()));
            assetChange.setSourceId(borrowCollection.getId());
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error("补发失败:", e);
            }
            int lateDays = DateHelper.diffInDays(DateHelper.beginOfDate(DateHelper.addHours(new Date(), 3)),
                    DateHelper.beginOfDate(borrowCollection.getCollectionAt()), false);
            long overPricipal = new Double(MoneyHelper.round(MoneyHelper.multiply(MoneyHelper.multiply(borrowCollection.getPrincipal(), 0.001), lateDays), 0)).longValue();  // 每天逾期费

            assetChange = new AssetChange();
            assetChange.setType(AssetChangeTypeEnum.receivedPaymentsPenalty);  // 收取逾期管理费
            assetChange.setUserId(borrowCollection.getUserId());
            assetChange.setForUserId(0l);
            assetChange.setMoney(overPricipal);
            assetChange.setRemark(String.format("收取借款标的[%s]逾期管理费%s元(补发)", borrow.getName(), StringHelper.formatDouble(overPricipal / 100D, false)));
            assetChange.setSeqNo(assetChangeProvider.getSeqNo());
            assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
            try {
                assetChangeProvider.commonAssetChange(assetChange);
            } catch (Exception e) {
                log.error("补发失败:", e);
            }

            //3.发送红包
            VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
            voucherPayRequest.setAccountId(redpackAccount.getAccountId());
            voucherPayRequest.setTxAmount(StringHelper.formatDouble(borrowCollection.getCollectionMoney() + overPricipal - interestFee, 100, false));
            voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
            voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
            voucherPayRequest.setDesLine("债权转让回款补发!");
            voucherPayRequest.setChannel(ChannelContant.HTML);
            VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
            if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                log.error("redPacket" + msg);
            }
        }
    }

    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/batch/deal")
    @Transactional
    public void batchDeal(@RequestParam("sourceId") Object sourceId, @RequestParam("batchNo") Object batchNo) {
       /* Map<String,Object> acqMap = new HashMap<>();
        acqMap.put("borrowId", 169979);
        acqMap.put("tag", MqTagEnum.END_CREDIT_BY_TRANSFER);*/
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", StringHelper.toString(sourceId))
                .eq("batchNo", StringHelper.toString(batchNo))
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
        if (CollectionUtils.isEmpty(thirdBatchLogList)) {
            return;
        }

       /* try {
            //批次执行问题
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(sourceId), StringHelper.toString(batchNo),
                    thirdBatchLogList.get(0).getAcqRes(), "");
        } catch (Exception e) {
            log.error("批次执行异常:", e);
        }

        /*ThirdBatchLog thirdBatchLog = thirdBatchLogList.get(0);
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(thirdBatchLog.getSourceId()),
                        MqConfig.BATCH_NO, StringHelper.toString(thirdBatchLog.getBatchNo()),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()),
                        MqConfig.ACQ_RES, thirdBatchLogList.get(0).getAcqRes()
                );

        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq exception", e);
        }*/
    }

   /* @Transactional(rollbackFor = Exception.class)
    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/packet/send")
    public void redPacket() {
        long redpackAccountId = 0;
        try {
            redpackAccountId = assetChangeProvider.getRedpackAccountId();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        UserThirdAccount redpackAccount = userThirdAccountService.findByUserId(redpackAccountId);
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(22002l);

        long money = 160000*100l;
        // 发放理财师奖励
        AssetChange assetChange = new AssetChange();
        assetChange.setMoney(money);
        assetChange.setType(AssetChangeTypeEnum.receiveRedpack);  //  扣除红包
        assetChange.setUserId(userThirdAccount.getUserId());
        assetChange.setForUserId(userThirdAccount.getUserId());
        assetChange.setRemark(String.format("发放红包至zfh %s元", StringHelper.formatDouble(money / 100D, true)));
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setForUserId(redpackAccount.getUserId());
        assetChange.setSourceId(0L);
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            log.error("发送zfh红包失败:", e);
            return;
        }

        //3.发送红包
        VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
        voucherPayRequest.setAccountId(redpackAccount.getAccountId());
        voucherPayRequest.setTxAmount(StringHelper.formatDouble(money, 100, false));
        voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
        voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
        voucherPayRequest.setDesLine("红包发送!");
        voucherPayRequest.setChannel(ChannelContant.HTML);
        VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
            log.error("redPacket" + msg);
        }
    }*/

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

/*

    @ApiOperation("解除冻结")
    @RequestMapping("/pub/cancelFreeze")
    @Transactional
    public void cancelFreeze() {
        // 取消冻结
        AssetChange assetChange = new AssetChange();
        assetChange.setSourceId(279867l);
        assetChange.setGroupSeqNo(assetChangeProvider.getGroupSeqNo());
        assetChange.setMoney(5000);
        assetChange.setSeqNo(assetChangeProvider.getSeqNo());
        assetChange.setRemark(String.format("存管系统审核投资标的[老猪，12天]资格失败, 解除资金冻结50元"));
        assetChange.setType(AssetChangeTypeEnum.unfreeze);
        assetChange.setUserId(129659l);
        assetChange.setForUserId(129659l);
        try {
            assetChangeProvider.commonAssetChange(assetChange);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

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
