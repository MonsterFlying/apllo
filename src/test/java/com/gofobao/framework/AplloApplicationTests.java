package com.gofobao.framework;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.CertHelper;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Request;
import com.gofobao.framework.api.model.account_details_query2.AccountDetailsQuery2Response;
import com.gofobao.framework.api.model.account_query_by_mobile.AccountQueryByMobileRequest;
import com.gofobao.framework.api.model.account_query_by_mobile.AccountQueryByMobileResponse;
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
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryReq;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryResp;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResponse;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryRequest;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryResponse;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoQueryThirdBorrowList;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LeSpecification;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.listener.providers.CreditProvider;
import com.gofobao.framework.listener.providers.TransferProvider;
import com.gofobao.framework.marketing.biz.MarketingProcessBiz;
import com.gofobao.framework.member.biz.BrokerBounsBiz;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.biz.impl.WebUserThirdBizImpl;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.migrate.MigrateBorrowBiz;
import com.gofobao.framework.migrate.MigrateProtocolBiz;
import com.gofobao.framework.repayment.biz.LoanBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.scheduler.DailyAssetBackupScheduler;
import com.gofobao.framework.scheduler.DealThirdBatchScheduler;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.service.IncrStatisticService;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.gofobao.framework.wheel.borrow.biz.WheelBorrowBiz;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AplloApplicationTests {
    final Gson GSON = new GsonBuilder().create();

    @Autowired
    MqHelper mqHelper;

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private BorrowProvider borrowProvider;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;
    @Autowired
    private RepaymentBiz repaymentBiz;
    @Autowired
    private TransferBuyLogService transferBuyLogService;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    JixinTxDateHelper jixinTxDateHelper;
    @Autowired
    private BorrowCollectionService borrowCollectionService;

    @Autowired
    AssetService assetService;
    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    FundStatisticsBiz fundStatisticsBiz;

    @Autowired
    UserService userService;

    @Autowired
    MarketingProcessBiz marketingProcessBiz;

    @Autowired
    MigrateBorrowBiz migrateBorrowBiz;

    @Autowired
    MigrateProtocolBiz migrateProtocolBiz;

    @Test
    public void testMigrateBiz() {
        migrateBorrowBiz.getBorrowMigrateFile();
    }

    @Test
    public void testMigrateProtocolBiz() {
        migrateProtocolBiz.getProtocolMigrateFile();
    }


    @Test
    public void testDownloadFile() throws Exception {
        fundStatisticsBiz.doEve("");
    }


    @Autowired
    UserThirdAccountService userThirdAccountServices;

    @Autowired
    WebUserThirdBizImpl webUserThirdBiz;

    @Test
    public void touchMarketing() {
        Tender tender = tenderService.findById(262285L);
        borrowBiz.touchMarketingByTender(tender);

    }

    @Test
    public void contextLoads() throws InterruptedException {
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TENDER);
        mqConfig.setTag(MqTagEnum.AUTO_TENDER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString("169782"), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("borrowProvider autoTender send mq exception", e);
        }

        Thread.sleep(100000 * 1000);
    }


    public AccountQueryByMobileResponse findAccountByMobile() {
        AccountQueryByMobileRequest request = new AccountQueryByMobileRequest();
        request.setMobile("18949830519");
        request.setChannel(ChannelContant.HTML);
        AccountQueryByMobileResponse response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_QUERY_BY_MOBILE, request, AccountQueryByMobileResponse.class);
        return response;
    }

    public void trusteePay() {
        TrusteePayQueryReq request = new TrusteePayQueryReq();
        request.setAccountId("6212462040000250045");
        request.setProductId("GA69761");
        request.setChannel(ChannelContant.HTML);
        TrusteePayQueryResp response = jixinManager.send(JixinTxCodeEnum.TRUSTEE_PAY_QUERY, request, TrusteePayQueryResp.class);
        System.out.println(response);
    }

    public void creditAuthQuery() {
        CreditAuthQueryRequest request = new CreditAuthQueryRequest();
        request.setAccountId("6212462040000300030");
        request.setType("1");
        request.setChannel(ChannelContant.HTML);
        CreditAuthQueryResponse response = jixinManager.send(JixinTxCodeEnum.CREDIT_AUTH_QUERY, request, CreditAuthQueryResponse.class);
        System.out.println(response);
    }

    public void batchCancel() {
        BatchCancelReq request = new BatchCancelReq();
        request.setBatchNo("100002");
        request.setTxAmount("1000.00");
        request.setTxCounts("1");
        request.setChannel(ChannelContant.HTML);
        BatchCancelResp response = jixinManager.send(JixinTxCodeEnum.BATCH_CANCEL, request, BatchCancelResp.class);
        System.out.println(response);
    }

    public void creditInvestQuery() {
        CreditInvestQueryReq request = new CreditInvestQueryReq();
        request.setChannel(ChannelContant.HTML);
        request.setAccountId("6212462040000000077");
        request.setOrgOrderId("GFBLP_1498557194741");
        request.setAcqRes("1");
        CreditInvestQueryResp response = jixinManager.send(JixinTxCodeEnum.CREDIT_INVEST_QUERY, request, CreditInvestQueryResp.class);
        System.out.println(response);
    }


    private void doFirstVerify() {
        try {
            borrowBiz.doFirstVerify(169853L);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void findThirdBorrowList() {
        VoQueryThirdBorrowList voQueryThirdBorrowList = new VoQueryThirdBorrowList();
        voQueryThirdBorrowList.setProductId("170087");
        voQueryThirdBorrowList.setUserId(45126L);
        voQueryThirdBorrowList.setPageNum("1");
        voQueryThirdBorrowList.setPageSize("10");
        DebtDetailsQueryResponse resp = borrowThirdBiz.queryThirdBorrowList(voQueryThirdBorrowList);
        System.out.println(resp);


    }

    private void doAgainVerify() {
        Map<String, String> msg = new HashMap<>();
        msg.put("borrowId", "169983");
        try {
            borrowProvider.doAgainVerify(msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void batchDetailsQuery() {
        BatchDetailsQueryReq batchDetailsQueryReq = new BatchDetailsQueryReq();
        batchDetailsQueryReq.setBatchNo("173642");

        batchDetailsQueryReq.setBatchTxDate("20171031");
        batchDetailsQueryReq.setType("0");
        batchDetailsQueryReq.setPageNum("1");
        batchDetailsQueryReq.setPageSize("10");
        batchDetailsQueryReq.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, batchDetailsQueryReq, BatchDetailsQueryResp.class);
        if ((ObjectUtils.isEmpty(batchDetailsQueryResp)) || (!JixinResultContants.SUCCESS.equals(batchDetailsQueryResp.getRetCode()))) {
            log.error(ObjectUtils.isEmpty(batchDetailsQueryResp) ? "当前网络不稳定，请稍候重试" : batchDetailsQueryResp.getRetMsg());
        }
        log.info(GSON.toJson(batchDetailsQueryResp));
    }

    private void bidApplyQuery() {
        BidApplyQueryRequest request = new BidApplyQueryRequest();
        request.setAccountId("6212462190000058565");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_1504058244169462574939");
        BidApplyQueryResponse response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResponse.class);
        System.out.println(response);
    }


    @Autowired
    CertHelper certHelper;

    public void balanceQuery() {
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId("6212462190000059514");
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        System.out.println(balanceQueryResponse);
    }

    public void freezeDetailsQuery() {
        FreezeDetailsQueryRequest freezeDetailsQueryRequest = new FreezeDetailsQueryRequest();
        freezeDetailsQueryRequest.setChannel(ChannelContant.HTML);
        freezeDetailsQueryRequest.setState("1");
        freezeDetailsQueryRequest.setStartDate("201709014");
        freezeDetailsQueryRequest.setEndDate("201709015");
        freezeDetailsQueryRequest.setPageNum("1");
        freezeDetailsQueryRequest.setPageSize("20");
        freezeDetailsQueryRequest.setAccountId("6212462190000000401");

        FreezeDetailsQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.FREEZE_DETAILS_QUERY, freezeDetailsQueryRequest, FreezeDetailsQueryResponse.class);
        System.out.println(balanceQueryResponse);
    }


    public void accountDetailsQuery2() {

        AccountDetailsQuery2Request request = new AccountDetailsQuery2Request();
        request.setAccountId("6212462190000090196");
        request.setStartDate("20171002");
        request.setEndDate("20171207");
        request.setChannel(ChannelContant.HTML);
        request.setType("0"); // 转入
        AccountDetailsQuery2Response response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY2, request, AccountDetailsQuery2Response.class);
        System.out.println(response);
    }

    /**
     * 复审债权转让的
     */
    @Test
    public void testAgantTransfer() {
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TRANSFER);
        mqConfig.setTag(MqTagEnum.AGAIN_VERIFY_TRANSFER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(5708), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        log.info(String.format("transferBizImpl buyTransfer send mq %s", GSON.toJson(body)));
        mqHelper.convertAndSend(mqConfig);
    }

    public void batchQuery() {
        BatchQueryReq req = new BatchQueryReq();
        req.setChannel(ChannelContant.HTML);
        req.setBatchNo("142108");
        req.setBatchTxDate("20170901");
        BatchQueryResp resp = jixinManager.send(JixinTxCodeEnum.BATCH_QUERY, req, BatchQueryResp.class);
        System.out.println(resp);

    }


    /**
     * 解冻用户余额
     */
    public void unfrozee() {
        //解除存管资金冻结
        BalanceUnfreezeReq balanceUnfreezeReq = new BalanceUnfreezeReq();
        balanceUnfreezeReq.setAccountId("6212462190000004254");
        balanceUnfreezeReq.setTxAmount("10000");
        balanceUnfreezeReq.setChannel(ChannelContant.HTML);
        balanceUnfreezeReq.setOrderId("GFBBF_1504073566815264200331");
        balanceUnfreezeReq.setOrgOrderId("GFBBF_1504073566815264200330");
        BalanceUnfreezeResp balanceUnfreezeResp = jixinManager.send(JixinTxCodeEnum.BALANCE_UN_FREEZE, balanceUnfreezeReq, BalanceUnfreezeResp.class);
        if ((ObjectUtils.isEmpty(balanceUnfreezeResp)) || (!JixinResultContants.SUCCESS.equalsIgnoreCase(balanceUnfreezeResp.getRetCode()))) {
            log.error("失败");
        }
        log.info("成功");

    }


    public void batchDeal() {
       /* Map<String,Object> acqMap = new HashMap<>();
        acqMap.put("borrowId", 169979);
        acqMap.put("tag", MqTagEnum.END_CREDIT_BY_TRANSFER);*/

        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(170106),
                        MqConfig.BATCH_NO, StringHelper.toString("193522"),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()),
                        MqConfig.ACQ_RES, "{\"transferId\":5707}"
                );

        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq exception", e);
        }
    }

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    @Autowired
    private CreditProvider creditProvider;

    @Autowired
    private JixinHelper jixinHelper;

    @Autowired
    private DealThirdBatchScheduler dealThirdBatchScheduler;


    @Autowired
    private AssetChangeProvider assetChangeProvider;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private DailyAssetBackupScheduler dailyAssetBackupScheduler;
    @Autowired
    private NewAssetLogService newAssetLogService;
    @Autowired
    TransferProvider transferProvider;


    @Autowired
    private TransferBiz transferBiz;

    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private TenderService tenderService;

    @Autowired
    private ThirdBatchDealLogBiz thirdBatchDealLogBiz;


    @Test
    @Transactional
    public void test() {

        Date flagAt = new Date();
        flagAt = DateHelper.beginOfDate(flagAt);
        flagAt = DateHelper.subDays(flagAt, 2);

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("transferFlag", 0)
                .eq("status", 0)
                .predicate(new LeSpecification<>("collectionAt", new DataObject(flagAt)))
                .build();
        long count = borrowCollectionService.count(bcs);

        System.out.println(count);
        System.out.println(DateHelper.dateToString(flagAt));
        /* //批次处理
        batchDeal();
        //unfrozee();
        //查询存管账户资金信息
        balanceQuery();
        //查询资金流水
        accountDetailsQuery();*/
        //testCredit();
        //根据手机号查询存管账户
        //findAccountByMobile();
        //受托支付
        //trusteePay();
        //签约查询
        //creditAuthQuery();
        //取消批次
        //batchCancel();
        //查询投资人购买债权
        //creditInvestQuery();
        //垫付回调
        //advanceCall();
        //初审
        // doFirstVerify();
        //还款处理
        // repayDeal();
        //查询标的集合
        // findThirdBorrowList();
        //复审
        // doAgainVerify();
        //批次状态查询
        //batchQuery();
        //freezeDetailsQuery();
        //批次详情查询
        //batchDetailsQuery();
        //查询投标申请
        //bidApplyQuery();
        //转让标复审回调
        //transferBorrowAgainVerify();
        //非转让标复审问题
        //noTransferBorrowAgainVerify();
        // 查询债权关系
/*        CreditDetailsQueryRequest creditDetailsQueryRequest = new CreditDetailsQueryRequest();
        creditDetailsQueryRequest.setAccountId("6212462190000059118");
        creditDetailsQueryRequest.setStartDate("20170417");
        creditDetailsQueryRequest.setProductId("170138");
        creditDetailsQueryRequest.setEndDate(DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM));
        creditDetailsQueryRequest.setState("0");
        creditDetailsQueryRequest.setPageNum("1");
        creditDetailsQueryRequest.setPageSize("10");
        CreditDetailsQueryResponse creditDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.CREDIT_DETAILS_QUERY,
                creditDetailsQueryRequest,
                CreditDetailsQueryResponse.class);
        System.out.println(creditDetailsQueryResponse);*/

        /*VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setRepaymentId(1l);
        voRepayReq.setUserId(44912L);
        try {
            repaymentBiz.newRepay(voRepayReq);
        } catch (Exception e) {
            e.printStackTrace();
        }

        */

        /*CreditEndReq creditEndReq = new CreditEndReq();
        creditEndReq.setAccountId("6212462040000200123");
        creditEndReq.setProductId("169921");
        creditEndReq.setOrderId(JixinHelper.getOrderId(JixinHelper.END_CREDIT_PREFIX));
        creditEndReq.setForAccountId("6212462040000650087");
        creditEndReq.setAuthCode("20161011093802281461");
        creditEndReq.setChannel(ChannelContant.HTML);
        CreditEndResp creditEndResp = jixinManager.send(JixinTxCodeEnum.CREDIT_END,
                creditEndReq,
                CreditEndResp.class);
        System.out.println(creditEndResp);*/

       /* List<Repay> repayList = new ArrayList<>();
        Repay repay = new Repay();
        repay.setAccountId("6212462040000200123");
        repay.setOrderId(JixinHelper.getOrderId(JixinHelper.REPAY_PREFIX));
        repay.setTxAmount("1000");
        repay.setIntAmount("0");
        repay.setTxFeeIn("0");
        repay.setTxFeeOut("0");
        repay.setProductId("169921");
        repay.setAuthCode("20161011093649281458");
        repay.setForAccountId("6212462040000650087");
        repayList.add(repay);

        repay = new Repay();
        repay.setAccountId("6212462040000200123");
        repay.setOrderId(JixinHelper.getOrderId(JixinHelper.REPAY_PREFIX));
        repay.setTxAmount("1000");
        repay.setIntAmount("0");
        repay.setTxFeeIn("0");
        repay.setTxFeeOut("0");
        repay.setProductId("169921");
        repay.setAuthCode("20161011093802281461");
        repay.setForAccountId("6212462040000650087");
        repayList.add(repay);

        BatchRepayReq request = new BatchRepayReq();
        request.setBatchNo(jixinHelper.getBatchNo());
        request.setTxAmount("2000");
        request.setRetNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/repayDeal/run");
        request.setNotifyURL(javaDomain + "/pub/repayment/v2/third/batch/repayDeal/check");
        request.setSubPacks(GSON.toJson(repayList));
        request.setChannel(ChannelContant.HTML);
        request.setTxCounts(StringHelper.toString(repayList.size()));
        BatchRepayResp response = jixinManager.send(JixinTxCodeEnum.BATCH_REPAY, request, BatchRepayResp.class);
        System.out.println(response);*/
    }

    @Autowired
    private LoanBiz loanBiz;

    @Test
    public void timingRepayment() {
        loanBiz.timingRepayment(new Date());
    }

    @Autowired
    private IncrStatisticService incrStatisticService;

    @Test
    public void incrstatistic() {
        incrStatisticService.dayStatistic(new Date());
    }

    @Autowired
    private BrokerBounsBiz brokerBounsBiz;


}
