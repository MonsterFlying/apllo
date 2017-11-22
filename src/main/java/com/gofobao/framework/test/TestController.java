package com.gofobao.framework.test;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.account_id_query.AccountIdQueryRequest;
import com.gofobao.framework.api.model.account_id_query.AccountIdQueryResponse;
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
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryRequest;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResponse;
import com.gofobao.framework.api.model.card_bind_details_query.CardBindDetailsQueryRequest;
import com.gofobao.framework.api.model.card_bind_details_query.CardBindDetailsQueryResponse;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryRequest;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryResponse;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryReq;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryResp;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryRequest;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryResponse;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.helper.project.BatchAssetChangeHelper;
import com.gofobao.framework.helper.project.IntegralChangeHelper;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.member.vo.response.VoOpenAccountResp;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.biz.TenderBiz;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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

import java.util.*;

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

    @RequestMapping("/pub/test/asset/change")
    @Transactional(rollbackFor = Exception.class)
    public void repairCall(@RequestParam("sourceId") Object sourceId, @RequestParam("batchNo") Object batchNo, @RequestParam("type") Object type) {
        //2.处理资金还款人、收款人资金变动
        try {
            batchAssetChangeHelper.batchAssetChangeDeal(NumberHelper.toLong(sourceId), String.valueOf(batchNo), NumberHelper.toInt(type));
        } catch (Exception e) {
            log.error("变动异常：", e);
        }
    }


    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/batch/deal")
    @Transactional(rollbackFor = Exception.class)
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

    @Autowired
    TenderBiz tenderBiz;

    @ApiOperation("结束第三方债权")
    @RequestMapping("/pub/test/end/third")
    @Transactional(rollbackFor = Exception.class)
    public void endThird(@RequestParam("userId") Object userId) {
        ResponseEntity<VoBaseResp> responseEntity = null;
        try {
            responseEntity = tenderBiz.endThirdTender(NumberHelper.toLong(userId));
        } catch (Exception e) {
            log.error("异常：", e);
            e.printStackTrace();
        }
        log.info(responseEntity.getBody().getState().getMsg());
    }

    @ApiOperation("获取自动投标列表")
    @RequestMapping("/pub/credit/invest/query")
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> batchDeal() {
        return ResponseEntity.ok("success");
    }

    @ApiOperation("资产查询")
    @RequestMapping("/pub/asset/find")
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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


    @ApiOperation("用户债权列表查询")
    @RequestMapping("/pub/test/batch/cancel")
    @Transactional(rollbackFor = Exception.class)
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

    @ApiOperation("个人账户银行卡查询")
    @RequestMapping("/pub/bank/find")
    @Transactional(rollbackFor = Exception.class)
    public void bankQuery(@RequestParam("accountId") Object accountId) {
        CardBindDetailsQueryRequest cardBindDetailsQueryRequest = new CardBindDetailsQueryRequest();
        cardBindDetailsQueryRequest.setAccountId(String.valueOf(accountId));
        cardBindDetailsQueryRequest.setState("1");
        CardBindDetailsQueryResponse cardBindDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.CARD_BIND_DETAILS_QUERY,
                cardBindDetailsQueryRequest,
                CardBindDetailsQueryResponse.class);

        log.info("=========================================================================================");
        log.info("个人账户银行卡查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(cardBindDetailsQueryResponse));
    }

    @ApiOperation("个人账户查询")
    @RequestMapping("/pub/account/find")
    @Transactional(rollbackFor = Exception.class)
    public void accountQuery(@RequestParam("idNo") Object idNo) {
        AccountIdQueryRequest accountIdQueryRequest = new AccountIdQueryRequest();
        accountIdQueryRequest.setIdNo(StringHelper.toString(idNo));
        AccountIdQueryResponse accountIdQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_ID_QUERY,
                accountIdQueryRequest, AccountIdQueryResponse.class);
        log.info("=========================================================================================");
        log.info("个人账户查询:");
        log.info("=========================================================================================");
        log.info(GSON.toJson(accountIdQueryResponse));
    }

    @ApiOperation("批次查询")
    @RequestMapping("/pub/batch/find")
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
