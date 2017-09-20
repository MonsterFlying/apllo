package com.gofobao.framework;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.CertHelper;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
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
import com.gofobao.framework.api.model.batch_lend_pay.LendPay;
import com.gofobao.framework.api.model.batch_query.BatchQueryReq;
import com.gofobao.framework.api.model.batch_query.BatchQueryResp;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryReq;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResp;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryReq;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryResp;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResponse;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryRequest;
import com.gofobao.framework.api.model.freeze_details_query.FreezeDetailsQueryResponse;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.asset.contants.BatchAssetChangeContants;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoQueryThirdBorrowList;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.listener.providers.CreditProvider;
import com.gofobao.framework.marketing.biz.MarketingProcessBiz;
import com.gofobao.framework.member.biz.impl.WebUserThirdBizImpl;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserCacheService;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.migrate.MigrateBorrowBiz;
import com.gofobao.framework.migrate.MigrateProtocolBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.scheduler.DealThirdBatchScheduler;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchDealLogContants;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchDealLog;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.contants.BorrowContants;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.base.Preconditions;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class AplloApplicationTests {
    final Gson GSON = new GsonBuilder().create();

    @Autowired
    MqHelper mqHelper;

    @Autowired
    private BorrowBiz borrowBiz;
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
    private TenderService tenderService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ThirdBatchDealLogBiz thirdBatchDealLogBiz;

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
    public void testQueryFeeAccount() {
        Users users = userService.findById(45217L);
        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(users.getId());
        int pageSize = 20, pageIndex = 1, realSize = 0;
        String accountId = userThirdAccount.getAccountId();  // 存管账户ID
        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>();
        do {
            AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
            accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
            accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
            accountDetailsQueryRequest.setStartDate(""); // 查询当天数据
            accountDetailsQueryRequest.setEndDate("20160927");
            accountDetailsQueryRequest.setType("0");
            accountDetailsQueryRequest.setAccountId(accountId);

            AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                    accountDetailsQueryRequest,
                    AccountDetailsQueryResponse.class);

            if ((ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg();
                log.error(msg);
                break;
            }

            String subPacks = accountDetailsQueryResponse.getSubPacks();
            if (StringUtils.isEmpty(subPacks)) {
                break;
            }

            Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
            }.getType()));
            List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(com.google.common.collect.Lists.newArrayList());
            realSize = accountDetailsQueryItems.size();
            accountDetailsQueryItemList.addAll(accountDetailsQueryItems);
            pageIndex++;
        } while (realSize == pageSize);

        for (AccountDetailsQueryItem item : accountDetailsQueryItemList) {
            log.error(GSON.toJson(item));
        }
    }


    @Test
    public void testQueryUserInfo() {
        List<UserThirdAccount> userThirdAccountList = userThirdAccountService.findByAll();

        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>();
        for (UserThirdAccount userThirdAccount : userThirdAccountList) {
            // 查询当天充值记录
            int pageSize = 20, pageIndex = 1, realSize = 0;
            String accountId = userThirdAccount.getAccountId();  // 存管账户ID
            do {
                AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
                accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
                accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
                accountDetailsQueryRequest.setStartDate(jixinTxDateHelper.getTxDateStr()); // 查询当天数据
                accountDetailsQueryRequest.setEndDate(jixinTxDateHelper.getTxDateStr());
                accountDetailsQueryRequest.setType("0");
                accountDetailsQueryRequest.setAccountId(accountId);

                AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY,
                        accountDetailsQueryRequest,
                        AccountDetailsQueryResponse.class);

                if ((ObjectUtils.isEmpty(accountDetailsQueryResponse)) || (!JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()))) {
                    String msg = ObjectUtils.isEmpty(accountDetailsQueryResponse) ? "当前网络出现异常, 请稍后尝试！" : accountDetailsQueryResponse.getRetMsg();
                    log.error(msg);
                    break;
                }

                String subPacks = accountDetailsQueryResponse.getSubPacks();
                if (StringUtils.isEmpty(subPacks)) {
                    break;
                }

                Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
                }.getType()));
                List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(com.google.common.collect.Lists.newArrayList());
                realSize = accountDetailsQueryItems.size();
                accountDetailsQueryItemList.addAll(accountDetailsQueryItems);
                pageIndex++;
            } while (realSize == pageSize);
        }

        for (AccountDetailsQueryItem item : accountDetailsQueryItemList) {
            log.error(GSON.toJson(item));
        }

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
        batchDetailsQueryReq.setBatchNo("093205");

        batchDetailsQueryReq.setBatchTxDate("20170918");
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
        BidApplyQueryReq request = new BidApplyQueryReq();
        request.setAccountId("6212462190000058565");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_1504058244169462574939");
        BidApplyQueryResp response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResp.class);
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

    @Test
    public void accountDetailsQuery() {

        AccountDetailsQueryRequest request = new AccountDetailsQueryRequest();
        request.setAccountId("6212462190000059514");
        request.setStartDate("20161002");
        request.setEndDate("20171003");
        request.setChannel(ChannelContant.HTML);
        request.setType("0"); // 转入
        //request.setTranType("7820"); // 线下转账的
        request.setPageSize(String.valueOf(30));
        request.setPageNum(String.valueOf(1));
        AccountDetailsQueryResponse response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY, request, AccountDetailsQueryResponse.class);
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

    public void testCredit() {
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TRANSFER);
        mqConfig.setTag(MqTagEnum.AGAIN_VERIFY_TRANSFER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(5708), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        log.info(String.format("transferBizImpl buyTransfer send mq %s", GSON.toJson(body)));
        mqHelper.convertAndSend(mqConfig);
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

    /**
     * 项目回款短信通知
     *
     * @param borrowCollectionList
     * @param parentBorrow
     * @param borrowRepayment
     */
    private void smsNoticeByReceivedRepay(List<BorrowCollection> borrowCollectionList, Borrow parentBorrow, BorrowRepayment borrowRepayment) {
        try {
            Set<Long> tenderIds = borrowCollectionList.stream().map(borrowCollection -> borrowCollection.getTenderId()).collect(Collectors.toSet()); /* 回款用户id */
            if (CollectionUtils.isEmpty(tenderIds)) {
                log.error("回款投标记录ID为空");
                return;
            }

            Specification<Tender> tenderSpecification = Specifications.
                    <Tender>and()
                    .in("id", tenderIds.toArray())
                    .build();
            List<Tender> tenderList = tenderService.findList(tenderSpecification);
            if (CollectionUtils.isEmpty(tenderList)) {
                log.error("回款投标记录为空");
                return;
            }
            Set<Long> userIds = tenderList.stream().map(tender -> tender.getUserId()).collect(Collectors.toSet()); /* 回款用户id */
            if (CollectionUtils.isEmpty(userIds)) {
                log.error("回款用户ID为空");
                return;
            }


            Map<Long /* 投资会员id */, List<BorrowCollection>> borrowCollrctionMaps = borrowCollectionList.stream().collect(groupingBy(BorrowCollection::getUserId)); /* 回款记录集合 */
            Specification<Users> us = Specifications
                    .<Users>and()
                    .in("id", userIds.toArray())
                    .build();

            List<Users> usersList = userService.findList(us);/* 回款用户缓存记录列表 */
            Map<Long /* 投资会员id */, Users> userMaps = usersList.stream().collect(Collectors.toMap(Users::getId, Function.identity()));/* 回款用户记录列表*/
            userIds.stream().forEach(userId -> {
                List<BorrowCollection> borrowCollections = borrowCollrctionMaps.get(userId);/* 当前用户的所有回款 */
                Users users = userMaps.get(userId);//投资人会员记录
                long principal = borrowCollections.stream().mapToLong(BorrowCollection::getPrincipal).sum(); /* 当前用户的所有回款本金 */
                long collectionMoneyYes = borrowCollections.stream().mapToLong(BorrowCollection::getCollectionMoneyYes).sum();/* 当前用户的所有回款本金 */
                long interest = collectionMoneyYes - principal;/* 当前用户的所有回款本金 */
                String phone = users.getPhone();/* 投资人手机号 */
                String name = "";
                if (!ObjectUtils.isEmpty(phone)) {
                    MqConfig config = new MqConfig();
                    config.setQueue(MqQueueEnum.RABBITMQ_SMS);
                    config.setTag(MqTagEnum.SMS_RECEIVED_REPAY);
                    switch (parentBorrow.getType()) {
                        case BorrowContants.CE_DAI:
                            name = "车贷标";
                            break;
                        case BorrowContants.JING_ZHI:
                            name = "净值标";
                            break;
                        case BorrowContants.QU_DAO:
                            name = "渠道标";
                            break;
                        default:
                            name = "投标还款";
                    }
                    Map<String, String> body = new HashMap<>();
                    body.put(MqConfig.PHONE, phone);
                    body.put(MqConfig.IP, "127.0.0.1");
                    body.put(MqConfig.MSG_ID, StringHelper.toString(parentBorrow.getId()));
                    body.put(MqConfig.MSG_NAME, name);
                    body.put(MqConfig.MSG_ORDER, StringHelper.toString(borrowRepayment.getOrder() + 1));
                    body.put(MqConfig.MSG_MONEY, StringHelper.formatDouble(principal, 100, true));
                    body.put(MqConfig.MSG_INTEREST, StringHelper.formatDouble(interest, 100, true));
                    config.setMsg(body);

                    boolean state = mqHelper.convertAndSend(config);
                    if (!state) {
                        log.error(String.format("发送投资人收到还款短信失败:%s", config));
                    }
                }
            });
        } catch (Exception e) {
            log.error("回款发送短信失败", e);
        }

    }

    @Test
    public void test() {

        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TENDER);
        mqConfig.setTag(MqTagEnum.AUTO_TENDER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString("179939"), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("borrowProvider autoTender send mq exception", e);
        }

        //1.查询并判断还款记录是否存在!
       /* BorrowRepayment borrowRepayment = borrowRepaymentService.findById(297l);*//* 当期还款记录 *//*
        Preconditions.checkNotNull(borrowRepayment, "还款记录不存在!");
        Borrow parentBorrow = borrowService.findById(borrowRepayment.getBorrowId());*//* 还款记录对应的借款记录 *//*
        Preconditions.checkNotNull(parentBorrow, "借款记录不存在!");
        *//* 还款对应的投标记录  包括债权转让在里面 *//*
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .eq("status", 1)
                .eq("borrowId", parentBorrow.getId())
                .build();
        List<Tender> tenderList = tenderService.findList(ts);*//* 还款对应的投标记录  包括债权转让在里面 *//*
        Preconditions.checkNotNull(tenderList, "立即还款: 投标记录为空!");
        *//* 投标记录id *//*
        Set<Long> tenderIds = tenderList.stream().map(tender -> tender.getId()).collect(Collectors.toSet());
        *//* 查询未转让的投标记录回款记录 *//*
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", tenderIds.toArray())
                .eq("status", 0)
                .eq("order", borrowRepayment.getOrder())
                .eq("transferFlag", 0)
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        Preconditions.checkNotNull(borrowCollectionList, "立即还款: 回款记录为空!");
        *//* 是否垫付 *//*
        boolean advance = !ObjectUtils.isEmpty(borrowRepayment.getAdvanceAtYes());
        if (!advance) { //非转让标需要统计与发放短信
            //10.项目回款短信通知
            smsNoticeByReceivedRepay(borrowCollectionList, parentBorrow, borrowRepayment);
        }*/

     /*   //记录批次处理日志
        thirdBatchDealLogBiz.recordThirdBatchDealLog(String.valueOf(113841),169974, ThirdBatchDealLogContants.PROCESSED,true,
                ThirdBatchLogContants.BATCH_LEND_REPAY, "");
*/
        /*MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TENDER);
        mqConfig.setTag(MqTagEnum.AUTO_TENDER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString("170190"), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("borrowProvider autoTender send mq exception", e);
        }*/

        /*BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                NumberHelper.toDouble(StringHelper.toString(9)),
                NumberHelper.toDouble(StringHelper.toString(1500)), 12, new Date());
        Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(0);
        List<Map<String, Object>> repayDetailList = (List<Map<String, Object>>) rsMap.get("repayDetailList");
        System.out.println(repayDetailList);*/
/*        Borrow borrow = borrowService.findById(170185l);
        UserCache userCache = userCacheService.findById(22002l);
        Date nowDate = new Date();
        Date releaseAt = borrow.getReleaseAt();

        if (borrow.getIsNovice()) {  // 新手
            releaseAt = DateHelper.max(DateHelper.addHours(DateHelper.beginOfDate(releaseAt), 20), borrow.getReleaseAt());
        }
        if (ObjectUtils.isEmpty(borrow.getLendId())  && releaseAt.getTime() > nowDate.getTime() && !userCache.isNovice()) {
            log.info(String.valueOf(ObjectUtils.isEmpty(borrow.getLendId())));
            log.info(String.valueOf(releaseAt.getTime() > nowDate.getTime()));
            log.info(String.valueOf(!userCache.isNovice()));
        }*/

        /*Borrow borrow = new Borrow();
        long takeUserId = borrow.getTakeUserId();
        if (ObjectUtils.isEmpty(takeUserId)){

        }*/
        /*MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TENDER);
        mqConfig.setTag(MqTagEnum.AUTO_TENDER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString("170183"), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("borrowProvider autoTender send mq exception", e);
        }*/
        /*long redpackAccountId = 0;
        try {
            redpackAccountId = assetChangeProvider.getRedpackAccountId();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(redpackAccountId);
        UserThirdAccount userThirdAccount1 = userThirdAccountService.findByUserId(45255l);
        //3.发送红包
        VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
        voucherPayRequest.setAccountId(userThirdAccount.getAccountId());
        voucherPayRequest.setTxAmount("10.22");
        voucherPayRequest.setForAccountId(userThirdAccount1.getAccountId());
        voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
        voucherPayRequest.setDesLine("数据迁移账户初始化！");
        voucherPayRequest.setChannel(ChannelContant.HTML);
        VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
        if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
            String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
        }*/

   /*   //推送队列结束债权
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
        mqConfig.setTag(MqTagEnum.END_CREDIT);
        mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 1));
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(170106), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("thirdBatchProvider creditInvestDeal send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("thirdBatchProvider creditInvestDeal send mq exception", e);
        }*/

        //dealThirdBatchScheduler.process();
        //                  dataMigration();


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


}
