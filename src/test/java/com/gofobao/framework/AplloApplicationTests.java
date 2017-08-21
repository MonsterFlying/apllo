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
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_credit_invest.CreditInvestRun;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.batch_query.BatchQueryReq;
import com.gofobao.framework.api.model.batch_query.BatchQueryResp;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryReq;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResp;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryReq;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryResp;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResponse;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoQueryThirdBorrowList;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.listener.providers.CreditProvider;
import com.gofobao.framework.marketing.biz.MarketingProcessBiz;
import com.gofobao.framework.marketing.entity.MarketingData;
import com.gofobao.framework.marketing.enums.MarketingTypeEnum;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.migrate.MigrateBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.entity.TransferBuyLog;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
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

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    JixinTxDateHelper jixinTxDateHelper;
    @Autowired
    private BorrowCollectionService borrowCollectionService;

    @Autowired
    MigrateBiz migrateBiz;

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

    @Test
    public void testMigrateBiz() {
        migrateBiz.getMemberMigrateFile();
    }

    /**
     * 发送注册红包
     */
    @Test
    public void testRegisterPublicRedpack() {
        MarketingData marketingData = new MarketingData();
        marketingData.setUserId(44799L);
        marketingData.setMarketingType(MarketingTypeEnum.REGISTER);
        marketingData.setSourceId(44799L);
        marketingData.setTransTime(new Date());
        try {
            marketingProcessBiz.process(new Gson().toJson(marketingData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoginPublicRedpack() {
        MarketingData marketingData = new MarketingData();
        marketingData.setUserId(44799L);
        marketingData.setMarketingType(MarketingTypeEnum.LOGIN);
        marketingData.setSourceId(44799L);
        marketingData.setTransTime(new Date());
        try {
            marketingProcessBiz.process(new Gson().toJson(marketingData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTenderPublicRedpack() {
        MarketingData marketingData = new MarketingData();
        marketingData.setUserId(44884L);
        marketingData.setMarketingType(MarketingTypeEnum.TENDER);
        marketingData.setSourceId(261540L);
        marketingData.setTransTime(new Date());
        try {
            marketingProcessBiz.process(new Gson().toJson(marketingData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testDownloadFile() throws Exception {
        fundStatisticsBiz.doEVE();
    }


    @Test
    public void testQueryFeeAccount() {
        Users users = userService.findById(20L);
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

    public static void main(String[] args) {
        List<Long> sumPrincipals = new ArrayList<>(2);
        List<Long> sumInterests = new ArrayList<>();
        long i = (sumInterests.size() - 2) != 1 ? 0 : sumPrincipals.get(2);

        /*BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(
                NumberHelper.toDouble(StringHelper.toString(999.9)),
                NumberHelper.toDouble(StringHelper.toString(2100)), 3, new Date());
        Map<String, Object> rsMap = borrowCalculatorHelper.simpleCount(0);
        System.out.println(rsMap);*/

        /*System.out.println("select t.id AS id,t. STATUS AS status,t.user_id AS userId,t.lowest AS lowest,t.borrow_types AS borrowTypes," +
                "t.repay_fashions AS repayFashions,t.tender_0 AS tender0,t.tender_1 AS tender1,t.tender_3 AS tender3,t.tender_4 AS tender4,t.`mode` AS mode,t.tender_money AS tenderMoney,t.timelimit_first AS timelimitFirst,t.timelimit_last AS timelimitLast,t.timelimit_type AS timelimitType,t.apr_first AS aprFirst,t.apr_last AS aprLast,t.save_money AS saveMoney,t.`order` AS `order`,t.auto_at AS autoAt,t.created_at AS createdAt," +
                "t.updated_at AS updatedAt,a.use_money AS useMoney,a.no_use_money AS noUseMoney,a.virtual_money AS virtualMoney,a.collection AS collection,a.payment AS payment " +
                "from gfb_auto_tender t " +
                "left join gfb_asset a on t.user_id = a.user_id " +
                "left join gfb_user_third_account uta on  t.user_id =  uta.user_id " +
                "where 1=1 and uta.del = 0 ");

        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();
        map.put("repaymentId", "173810");
        System.out.println(gson.toJson(map));
        System.out.println(SecurityHelper.getSign(gson.toJson(map)));*/
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

    @Transactional(rollbackOn = Exception.class)
    public void dataMigration() {
        //1查询债权借款
        String sql = "select id from gfb_borrow where tender_id > 0";
        List<Long> queryForList = (List<Long>) entityManager.createNativeQuery(sql.toString()).getResultList();
        /* 债权转让借款 */
        Specification<Borrow> bs = Specifications
                .<Borrow>and()
                .in("id", new HashSet<Long>(queryForList).toArray())
                .build();
        List<Borrow> transferBorrowList = borrowService.findList(bs);
        Map<Long, Borrow> transferBorrowMaps = transferBorrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        Set<Long> transferBorrowIds = transferBorrowList.stream().map(Borrow::getId).collect(Collectors.toSet());

        /* 转出债权集合 */
        Set<Long> transferTenderIds = transferBorrowList.stream().map(Borrow::getTenderId).collect(Collectors.toSet());
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .in("id", transferTenderIds.toArray())
                .build();
        List<Tender> transferTenderList = tenderService.findList(ts);
        Map<Long, Tender> transferTenderMaps = transferTenderList.stream().collect(Collectors.toMap(Tender::getId, Function.identity()));
        /* 原始标id */
        Set<Long> parentBorrowIds = transferTenderList.stream().map(Tender::getBorrowId).collect(Collectors.toSet());
        /* 原始借款集合 */
        bs = Specifications
                .<Borrow>and()
                .in("id", parentBorrowIds.toArray())
                .build();
        List<Borrow> parentBorrowList = borrowService.findList(bs);
        Map<Long, Borrow> parentBorrowMaps = parentBorrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
        /*转出债权回款集合*/
        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .in("tenderId", transferTenderIds.toArray())
                .eq("transferFlag", 1)
                .build();
        List<BorrowCollection> transferBorrowCollectionList = borrowCollectionService.findList(bcs, new Sort(Sort.Direction.ASC, "order"));
        Map<Long/* 投标id tender_id */, List<BorrowCollection>> transferBorrowCollectionMaps = transferBorrowCollectionList.stream().collect(groupingBy(BorrowCollection::getTenderId));
        List<Transfer> transferList = new ArrayList<>();
        transferBorrowList.stream().forEach(borrow -> {
            Tender transferTender = transferTenderMaps.get(borrow.getTenderId());
            List<BorrowCollection> transferBorrowCollection = transferBorrowCollectionMaps.get(transferTender.getId());
            boolean flag = !CollectionUtils.isEmpty(transferBorrowCollection);
            Borrow parentBorrow = parentBorrowMaps.get(transferTender.getBorrowId());

            Transfer transfer = new Transfer();
            transfer.setState(borrow.getStatus() == 5 ? 4 : (borrow.getStatus() == 2 ? 3 : 2));
            transfer.setTitle(borrow.getName());
            transfer.setSuccessAt(borrow.getSuccessAt());
            transfer.setUserId(borrow.getUserId());
            transfer.setType(0);
            transfer.setTransferMoney(borrow.getMoney());
            transfer.setTransferMoneyYes(borrow.getMoneyYes());
            transfer.setAlreadyInterest(0l);
            transfer.setApr(borrow.getApr());
            transfer.setTimeLimit(borrow.getTimeLimit());
            transfer.setIsLock(borrow.getIsLock());
            transfer.setIsAll(true);
            transfer.setTenderId(borrow.getTenderId());
            transfer.setBorrowId(parentBorrow.getId());
            if (flag) {
                transfer.setStartOrder(transferBorrowCollection.get(0).getOrder());
                transfer.setEndOrder(transferBorrowCollection.get(transferBorrowCollection.size() - 1).getOrder());
                transfer.setRepayAt(transferBorrowCollection.get(0).getCollectionAt());
            }
            transfer.setReleaseAt(borrow.getReleaseAt());
            transfer.setCreatedAt(borrow.getCreatedAt());
            transfer.setUpdatedAt(borrow.getUpdatedAt());
            transferList.add(transfer);
        });
        transferService.save(transferList);
        Map<Long, Transfer> transferMaps = transferList.stream().filter(transfer -> transfer.getState() == 2).collect(Collectors.toMap(Transfer::getTenderId, Function.identity()));

        /* 查询债权转让borrow的投标记录 */
        ts = Specifications
                .<Tender>and()
                .in("borrowId", transferBorrowIds.toArray())
                .eq("status", 1)
                .build();
        List<Tender> buyTransferTenderList = tenderService.findList(ts);
        List<TransferBuyLog> transferBuyLogList = new ArrayList<>();
        buyTransferTenderList.stream().forEach(buyTransferTender -> {
            Borrow transferBorrow = transferBorrowMaps.get(buyTransferTender.getBorrowId());
            Tender transferTender = transferTenderMaps.get(transferBorrow.getTenderId());
            Transfer transfer = transferMaps.get(transferTender.getId());

            TransferBuyLog transferBuyLog = new TransferBuyLog();
            transferBuyLog.setSource(0);
            transferBuyLog.setState(1);
            transferBuyLog.setUserId(buyTransferTender.getUserId());
            transferBuyLog.setUpdatedAt(buyTransferTender.getUpdatedAt());
            transferBuyLog.setType(0);
            transferBuyLog.setValidMoney(buyTransferTender.getValidMoney());
            transferBuyLog.setBuyMoney(buyTransferTender.getValidMoney());
            transferBuyLog.setAlreadyInterest(0l);
            transferBuyLog.setTransferId(transfer.getId());
            transferBuyLog.setPrincipal(buyTransferTender.getValidMoney());
            transferBuyLog.setCreatedAt(transfer.getCreatedAt());
            transferBuyLogList.add(transferBuyLog);
        });
        transferBuyLogService.save(transferBuyLogList);
        Map<Long, List<TransferBuyLog>> transferBuyMaps = transferBuyLogList.stream().collect(groupingBy(TransferBuyLog::getTransferId));
        buyTransferTenderList.stream().forEach(buyTransferTender -> {
            Borrow transferBorrow = transferBorrowMaps.get(buyTransferTender.getBorrowId());
            Tender transferTender = transferTenderMaps.get(transferBorrow.getTenderId());
            Transfer transfer = transferMaps.get(transferTender.getId());
            Borrow prarentBorrow = parentBorrowMaps.get(transferTender.getBorrowId());
            List<TransferBuyLog> transferBuyLogs = transferBuyMaps.get(transfer.getId());
            List<Tender> childTenderList = addChildTender(transfer.getCreatedAt(), transfer, transferTender, transferBuyLogs);

            addChildTenderCollection(transfer.getCreatedAt(), transfer, prarentBorrow, childTenderList);
        });


    }

    /**
     * 生成子级债权回款记录，标注老债权回款已经转出
     *
     * @param nowDate
     * @param transfer
     * @param parentBorrow
     * @param childTenderList
     */
    public List<BorrowCollection> addChildTenderCollection(Date nowDate, Transfer transfer, Borrow parentBorrow, List<Tender> childTenderList) {
        List<BorrowCollection> childTenderCollectionList = new ArrayList<>();/* 债权子记录回款记录 */
        String borrowCollectionIds = transfer.getBorrowCollectionIds();
        //生成子级债权回款记录，标注老债权回款已经转出
        Specification<BorrowCollection> bcs = null;
        if (transfer.getIsAll()) {
            bcs = Specifications
                    .<BorrowCollection>and()
                    .eq("tenderId", transfer.getTenderId())
                    .eq("id", borrowCollectionIds.split(","))
                    .eq("status", 0)
                    .build();
        } else {
            bcs = Specifications
                    .<BorrowCollection>and()
                    .eq("tenderId", transfer.getTenderId())
                    .eq("status", 0)
                    .build();
        }
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);/* 债权转让原投资回款记录 */
        long transferInterest = borrowCollectionList.stream().mapToLong(BorrowCollection::getInterest).sum();/* 债权转让总利息 */
        Date repayAt = transfer.getRepayAt();/* 原借款下一期还款日期 */
        Date startAt = DateHelper.subMonths(repayAt, 1);/* 计息开始时间 */
        long sumCollectionInterest = 0;//总回款利息
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
            Preconditions.checkNotNull(repayDetailList, "生成用户回款计划开始: 计划生成为空");
            BorrowCollection borrowCollection;
            long collectionMoney = 0;
            long collectionInterest = 0;
            int startOrder = borrowCollectionList.get(0).getOrder();/* 获取开始转让期数,期数下标从0开始 */
            for (int i = 0; i < repayDetailList.size(); i++) {
                borrowCollection = new BorrowCollection();
                Map<String, Object> repayDetailMap = repayDetailList.get(i);
                collectionMoney += new Double(NumberHelper.toDouble(repayDetailMap.get("repayMoney"))).longValue();
                long interest = new Double(NumberHelper.toDouble(repayDetailMap.get("interest"))).longValue();
                collectionInterest += interest;
                sumCollectionInterest += interest;
                //最后一个购买债权转让的最后一期回款，需要把还款溢出的利息补给新的回款记录
                if ((j == childTenderList.size() - 1) && (i == repayDetailList.size() - 1)) {
                    interest += transferInterest - sumCollectionInterest;/* 新的回款利息添加溢出的利息 */
                }

                borrowCollection.setTenderId(childTender.getId());
                borrowCollection.setStatus(0);
                borrowCollection.setOrder(startOrder++);
                borrowCollection.setUserId(childTender.getUserId());
                borrowCollection.setStartAt(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : startAt);
                borrowCollection.setStartAtYes(i > 0 ? DateHelper.stringToDate(StringHelper.toString(repayDetailList.get(i - 1).get("repayAt"))) : nowDate);
                borrowCollection.setCollectionAt(DateHelper.stringToDate(StringHelper.toString(repayDetailMap.get("repayAt"))));
                borrowCollection.setCollectionMoney(NumberHelper.toLong(repayDetailMap.get("repayMoney")));
                borrowCollection.setPrincipal(NumberHelper.toLong(repayDetailMap.get("principal")));
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

        //更新转出投资记录回款状态
        borrowCollectionList.stream().forEach(bc -> {
            bc.setTransferFlag(1);
        });
        borrowCollectionService.save(borrowCollectionList);

        return childTenderCollectionList;
    }

    /**
     * 新增子级标的
     *
     * @param nowDate
     * @param transfer
     * @param parentTender
     * @param transferBuyLogList
     * @return
     */
    public List<Tender> addChildTender(Date nowDate, Transfer transfer, Tender parentTender, List<TransferBuyLog> transferBuyLogList) {
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
            childTender.setState(2);
            childTender.setParentId(parentTender.getId());
            childTender.setTransferBuyId(transferBuyLog.getId());
            childTender.setAlreadyInterest(transferBuyLog.getAlreadyInterest());
            childTender.setThirdTransferOrderId(transferBuyLog.getThirdTransferOrderId());
            childTender.setThirdTransferFlag(transferBuyLog.getThirdTransferFlag());
            childTender.setTransferAuthCode(transferBuyLog.getTransferAuthCode());
            childTender.setCreatedAt(nowDate);
            childTender.setUpdatedAt(nowDate);
            childTenderList.add(childTender);

            //更新购买净值标状态为成功购买
            transferBuyLog.setState(1);
            transferBuyLog.setUpdatedAt(nowDate);
        });
        tenderService.save(childTenderList);
        transferBuyLogService.save(transferBuyLogList);

        //更新老债权为已转让
        parentTender.setTransferFlag(transfer.getIsAll() ? 3 : 2);
        parentTender.setUpdatedAt(nowDate);
        tenderService.save(parentTender);
        //更新债权转让为已转让
        transfer.setState(2);
        transfer.setUpdatedAt(nowDate);
        transferService.save(transfer);
        return childTenderList;
    }

    private void findThirdBorrowList() {
        VoQueryThirdBorrowList voQueryThirdBorrowList = new VoQueryThirdBorrowList();
        voQueryThirdBorrowList.setProductId("169917");
        voQueryThirdBorrowList.setUserId(39557L);
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
        batchDetailsQueryReq.setBatchNo("110355");
        batchDetailsQueryReq.setBatchTxDate("20170819");
        batchDetailsQueryReq.setType("0");
        batchDetailsQueryReq.setPageNum("1");
        batchDetailsQueryReq.setPageSize("10");
        batchDetailsQueryReq.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp batchDetailsQueryResp = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, batchDetailsQueryReq, BatchDetailsQueryResp.class);
        if ((ObjectUtils.isEmpty(batchDetailsQueryResp)) || (!JixinResultContants.SUCCESS.equals(batchDetailsQueryResp.getRetCode()))) {
            log.error(ObjectUtils.isEmpty(batchDetailsQueryResp) ? "当前网络不稳定，请稍候重试" : batchDetailsQueryResp.getRetMsg());
        }
    }

    private void bidApplyQuery() {
        BidApplyQueryReq request = new BidApplyQueryReq();
        request.setAccountId("6212462190000000476");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_150288481816451");
        BidApplyQueryResp response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResp.class);
        System.out.println(response);

    }

    private void noTransferBorrowAgainVerify() {
        Borrow borrow = borrowService.findById(169881L);
        try {
            borrowBiz.borrowAgainVerify(borrow);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Autowired
    CertHelper certHelper;

    public void saveThirdTransferAuthCode(List<CreditInvestRun> creditInvestRunList) {
        List<String> orderIds = creditInvestRunList.stream().map(creditInvestRun -> creditInvestRun.getOrderId()).collect(Collectors.toList());
        Specification<Tender> ts = Specifications
                .<Tender>and()
                .in("thirdTransferOrderId", orderIds.toArray())
                .build();

        List<Tender> tenderList = tenderService.findList(ts);
        Map<String, Tender> tenderMap = tenderList.stream().collect(Collectors.toMap(Tender::getThirdTransferOrderId, Function.identity()));
        creditInvestRunList.stream().forEach(creditInvestRun -> {
            String orderId = creditInvestRun.getOrderId();
            Tender tender = tenderMap.get(orderId);
            tender.setTransferAuthCode(creditInvestRun.getAuthCode());
        });
    }

    public void balanceQuery() {
        BalanceQueryRequest balanceQueryRequest = new BalanceQueryRequest();
        balanceQueryRequest.setChannel(ChannelContant.HTML);
        balanceQueryRequest.setAccountId("6212462190000000021");
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        System.out.println(balanceQueryResponse);
    }

    public void accountDetailsQuery() {
        AccountDetailsQueryRequest request = new AccountDetailsQueryRequest();
        request.setAccountId("6212462190000000310");
        request.setStartDate("20161002");
        request.setEndDate("20171003");
        request.setChannel(ChannelContant.HTML);
        request.setType("0"); // 转入
        //request.setTranType("7820"); // 线下转账的
        request.setPageSize(String.valueOf(20));
        request.setPageNum(String.valueOf(1));
        AccountDetailsQueryResponse response = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY, request, AccountDetailsQueryResponse.class);
        System.out.println(response);

    }

    public void batchQuery() {
        BatchQueryReq req = new BatchQueryReq();
        req.setChannel(ChannelContant.HTML);
        req.setBatchNo("173607");
        req.setBatchTxDate("20170718");
        BatchQueryResp resp = jixinManager.send(JixinTxCodeEnum.BATCH_QUERY, req, BatchQueryResp.class);
        System.out.println(resp);

    }

    public void batchDeal() {
       /* Map<String,Object> acqMap = new HashMap<>();
        acqMap.put("borrowId", 169979);
        acqMap.put("tag", MqTagEnum.END_CREDIT_BY_TRANSFER);*/

        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(170006),
                        MqConfig.BATCH_NO, StringHelper.toString(110355),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date())
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

    @Test
    public void test() {
        //dataMigration();

       /* MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_TRANSFER);
        mqConfig.setTag(MqTagEnum.AGAIN_VERIFY_TRANSFER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_TRANSFER_ID, StringHelper.toString(5), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {

            log.info(String.format("transferBizImpl buyTransfer send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("transferBizImpl buyTransfer send mq exception", e);
        }  */

        //推送队列结束债权
/*        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_CREDIT);
        mqConfig.setTag(MqTagEnum.END_CREDIT_ALL);
        mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 5));
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString(169919), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            log.info(String.format("repaymentBizImpl repayDeal send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("repaymentBizImpl repayDeal send mq exception", e);
        }*/

        //批次处理
        batchDeal();
        //查询存管账户资金信息
        //balanceQuery();
        //查询资金流水
        //accountDetailsQuery();
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
        //doFirstVerify();
        //还款处理
        //repayDeal();
        //查询标的集合
        //findThirdBorrowList();
        //复审
        //doAgainVerify();
        //批次状态查询
        //batchQuery();
        //批次详情查询
        //batchDetailsQuery();
        //查询投标申请
        //bidApplyQuery();
        //转让标复审回调
        //transferBorrowAgainVerify();
        //非转让标复审问题
        //noTransferBorrowAgainVerify();
        // 查询债权关系
        /*CreditDetailsQueryRequest creditDetailsQueryRequest = new CreditDetailsQueryRequest();
        creditDetailsQueryRequest.setAccountId("6212462190000000229");
        creditDetailsQueryRequest.setStartDate("20170417");
        creditDetailsQueryRequest.setProductId("169979");
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
        } 【原始技术】澳洲小哥真人MC：篱笆泥墙（1）

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
