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
import com.gofobao.framework.api.model.batch_repay.BatchRepayReq;
import com.gofobao.framework.api.model.batch_repay.BatchRepayResp;
import com.gofobao.framework.api.model.batch_repay.Repay;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryReq;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResp;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryRequest;
import com.gofobao.framework.api.model.credit_details_query.CreditDetailsQueryResponse;
import com.gofobao.framework.api.model.credit_end.CreditEndReq;
import com.gofobao.framework.api.model.credit_end.CreditEndResp;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryReq;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryResp;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResponse;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoQueryThirdBorrowList;
import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.BooleanHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.listener.providers.CreditProvider;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.VoAdvanceCall;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private JixinManager jixinManager;
    @Autowired
    private BorrowProvider borrowProvider;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;
    @Autowired
    private RepaymentBiz repaymentBiz;
    @Autowired
    private TenderService tenderService;

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    AssetService assetService;
    @Autowired
    UserThirdAccountService userThirdAccountService;

    @Autowired
    FundStatisticsBiz fundStatisticsBiz;


    @Autowired
    UserService userService;

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
        System.out.println(BooleanHelper.isFalse(null));

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

    private void advanceCall() {
        VoAdvanceCall voAdvanceCall = new VoAdvanceCall();
        voAdvanceCall.setRepaymentId(173795L);
        try {
            repaymentBiz.advanceDeal(voAdvanceCall);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void doFirstVerify() {
        try {
            borrowBiz.doFirstVerify(169853L);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void repayDeal() {
        VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setRepaymentId(173823L);
        voRepayReq.setUserId(37243L);
        voRepayReq.setIsUserOpen(false);
        voRepayReq.setInterestPercent(1d);
        try {
            repaymentBiz.repayDeal(voRepayReq);
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
        msg.put("borrowId", "169914");
        try {
            borrowProvider.doAgainVerify(msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void batchDetailsQuery() {
        BatchDetailsQueryReq batchDetailsQueryReq = new BatchDetailsQueryReq();
        batchDetailsQueryReq.setBatchNo("093919");
        batchDetailsQueryReq.setBatchTxDate("20170801");
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
        request.setAccountId("6212462040000650087");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_1501032966291");
        BidApplyQueryResp response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResp.class);
        System.out.println(response);

    }

    private void transferBorrowAgainVerify() {
        Borrow borrow = borrowService.findById(169860L);
        try {
            borrowBiz.transferBorrowAgainVerify(borrow);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void noTransferBorrowAgainVerify() {
        Borrow borrow = borrowService.findById(169881L);
        try {
            borrowBiz.notTransferBorrowAgainVerify(borrow);
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
        balanceQueryRequest.setAccountId("6212462040000650087");
        BalanceQueryResponse balanceQueryResponse = jixinManager.send(JixinTxCodeEnum.BALANCE_QUERY, balanceQueryRequest, BalanceQueryResponse.class);
        System.out.println(balanceQueryResponse);
    }

    public void accountDetailsQuery() {
        AccountDetailsQueryRequest request = new AccountDetailsQueryRequest();
        request.setAccountId("6212462040000750085");
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
        /*Map<String,Object> acqMap = new HashMap<>();
        acqMap.put("repaymentId","173855");
        acqMap.put("interestPercent","1d");
        acqMap.put("isUserOpen",true);
        acqMap.put("userId",44833);*/

        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, StringHelper.toString(169919),
                        MqConfig.BATCH_NO, StringHelper.toString(174806),
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date())
                       /* MqConfig.ACQ_RES, GSON.toJson(acqMap)*/
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
        /*Map<String,String> map = new HashMap<>();
        map.put(MqConfig.MSG_BORROW_ID,"169921");
        try {
            creditProvider.endThirdCredit(map,0);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
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
        //batchDeal();
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
        creditDetailsQueryRequest.setAccountId("6212462040000650087");
        creditDetailsQueryRequest.setStartDate("20161003");
        creditDetailsQueryRequest.setProductId("169922");
        creditDetailsQueryRequest.setEndDate(DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM));
        creditDetailsQueryRequest.setState("1");
        creditDetailsQueryRequest.setPageNum("1");
        creditDetailsQueryRequest.setPageSize("10");
        CreditDetailsQueryResponse creditDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.CREDIT_DETAILS_QUERY,
                creditDetailsQueryRequest,
                CreditDetailsQueryResponse.class);
        System.out.println(creditDetailsQueryResponse);*/


        CreditEndReq creditEndReq = new CreditEndReq();
        creditEndReq.setAccountId("6212462040000200123");
        creditEndReq.setProductId("169921");
        creditEndReq.setOrderId(JixinHelper.getOrderId(JixinHelper.END_CREDIT_PREFIX));
        creditEndReq.setForAccountId("6212462040000650087");
        creditEndReq.setAuthCode("20161011093802281461");
        creditEndReq.setChannel(ChannelContant.HTML);
        CreditEndResp creditEndResp = jixinManager.send(JixinTxCodeEnum.CREDIT_END,
                creditEndReq,
                CreditEndResp.class);
        System.out.println(creditEndResp);

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
