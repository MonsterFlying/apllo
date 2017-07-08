package com.gofobao.framework;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryItem;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryRequest;
import com.gofobao.framework.api.model.account_details_query.AccountDetailsQueryResponse;
import com.gofobao.framework.api.model.account_query_by_mobile.AccountQueryByMobileRequest;
import com.gofobao.framework.api.model.account_query_by_mobile.AccountQueryByMobileResponse;
import com.gofobao.framework.api.model.batch_bail_repay.BailRepayRun;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryReq;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryResp;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.VoAdvanceCall;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class AplloApplicationTests {

    final Gson GSON = new GsonBuilder().create();

    @Autowired
    MqHelper mqHelper;

    @Autowired
    private JixinHelper jixinHelper;

    @Autowired
    private UserThirdAccountService userThirdAccountService ;

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
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void contextLoads() throws InterruptedException {


        List<UserThirdAccount> UserThirdAccounts = userThirdAccountService.findByAll() ;
        List<AccountDetailsQueryItem> accountDetailsQueryItemList = new ArrayList<>() ;
        UserThirdAccount redpack  = new UserThirdAccount() ;
        redpack.setAccountId("6212461270000000444");
        UserThirdAccounts.add(redpack) ;
        UserThirdAccount fee = new UserThirdAccount() ;
        fee.setAccountId("6212461270000000360");
        UserThirdAccounts.add(fee) ;
        for(UserThirdAccount userThirdAccount : UserThirdAccounts){
            int pageIndex = 1, pageSize = 30, realSize ;


            Date nowDate = DateHelper.stringToDate("2016-09-22 23:59:59") ;
            Date startDate = DateHelper.subDays( nowDate, 30) ;

            do {
                AccountDetailsQueryRequest accountDetailsQueryRequest = new AccountDetailsQueryRequest();
                accountDetailsQueryRequest.setAccountId(userThirdAccount.getAccountId());
                accountDetailsQueryRequest.setType("0");
                accountDetailsQueryRequest.setEndDate(DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMD_NUM));
                accountDetailsQueryRequest.setStartDate(DateHelper.dateToString(startDate, DateHelper.DATE_FORMAT_YMD_NUM));
                // accountDetailsQueryRequest.setTranType("2616"); // cashType.equals(1) ? "2820" :
                accountDetailsQueryRequest.setPageNum(String.valueOf(pageIndex));
                accountDetailsQueryRequest.setPageSize(String.valueOf(pageSize));
                accountDetailsQueryRequest.setChannel(ChannelContant.HTML);
                AccountDetailsQueryResponse accountDetailsQueryResponse = jixinManager.send(JixinTxCodeEnum.ACCOUNT_DETAILS_QUERY, accountDetailsQueryRequest, AccountDetailsQueryResponse.class);
                Preconditions.checkNotNull(accountDetailsQueryResponse, "查询提现状态异常");
                Preconditions.checkArgument(JixinResultContants.SUCCESS.equals(accountDetailsQueryResponse.getRetCode()), "查询提现状态异常, 验证不通过");
                Optional<List<AccountDetailsQueryItem>> optional = Optional.ofNullable(GSON.fromJson(accountDetailsQueryResponse.getSubPacks(), new TypeToken<List<AccountDetailsQueryItem>>() {
                }.getType()));

                List<AccountDetailsQueryItem> accountDetailsQueryItems = optional.orElse(Lists.newArrayList());
                if(CollectionUtils.isEmpty(accountDetailsQueryItems)){
                    break;
                }

                accountDetailsQueryItemList.addAll(accountDetailsQueryItems) ;
                realSize = accountDetailsQueryItems.size();
                log.error(String.format("size %s", realSize) );
                pageIndex ++ ;
            }while (pageSize == realSize) ;
        }
        for(AccountDetailsQueryItem item : accountDetailsQueryItemList  ){
            System.err.println(new Gson().toJson(item));
        }

    }

    public static void main(String[] args) {
        Gson gson = new Gson();
        VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setUserId(2762L);
        voRepayReq.setRepaymentId(173795L);
        voRepayReq.setInterestPercent(0d);
        voRepayReq.setIsUserOpen(true);
        System.out.println(gson.toJson(voRepayReq));
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
        request.setAccountId("6212462040000250045");
        request.setOrgOrderId("GFBLP_1498557194741");
        request.setAcqRes("1");
        CreditInvestQueryResp response = jixinManager.send(JixinTxCodeEnum.CREDIT_INVEST_QUERY, request, CreditInvestQueryResp.class);
        System.out.println(response);
    }

    private void advanceCall() {
        VoAdvanceCall voAdvanceCall = new VoAdvanceCall();
        voAdvanceCall.setRepaymentId(173795L);
        voAdvanceCall.setBailRepayRunList(GSON.fromJson("[{\"accountId\":\"6212462040000000036\",\"authCode\":\"20160922115236083124\",\"productId\":\"GA69760\",\"orderId\":\"GFBBP_1499221906652\",\"failMsg\":\"交易成功\",\"txState\":\"S\",\"forAccountId\":\"6212462040000200040\",\"txAmount\":\"2021.92\"}]", new TypeToken<List<BailRepayRun>>() {
        }.getType()));
        try {
            repaymentBiz.advanceDeal(voAdvanceCall);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {

        Map<String,Object> map = jdbcTemplate.queryForMap("select count(id) as count from gfb_borrow_tender where borrow_id = 169794 and third_tender_order_id is not null");
        System.out.println(map.get("count"));

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

        /*Map<String,String> map = new HashMap<>();
        map.put("borrowId","169761");
        try {
            borrowProvider.doFirstVerify(map);
        } catch (Throwable e) {
            e.printStackTrace();
        }*/

        /*VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setRepaymentId(168683L);
        voRepayReq.setUserId(901L);
        voRepayReq.setIsUserOpen(false);
        voRepayReq.setInterestPercent(1d);
        try {
            repaymentBiz.repay(voRepayReq);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        System.out.println(true + "");*/

        /*VoQueryThirdBorrowList voQueryThirdBorrowList = new VoQueryThirdBorrowList();
        voQueryThirdBorrowList.setProductId("A69756");
        voQueryThirdBorrowList.setUserId(2762L);
        voQueryThirdBorrowList.setPageNum("1");
        voQueryThirdBorrowList.setPageSize("10");
        DebtDetailsQueryResp resp = borrowThirdBiz.queryThirdBorrowList(voQueryThirdBorrowList);
        System.out.println((resp.getTotalItems()));*/

        /*Map<String,String> msg = new HashMap<>();
        msg.put("borrowId","169760");
        try {
            borrowProvider.doAgainVerify(msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }*/

        //"userId\":901,\"repaymentId\":168675,\"interestPercent\":0.0,\"isUserOpen\":true
        /*VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setUserId(901L);
        voRepayReq.setRepaymentId(168675L);
        voRepayReq.setInterestPercent(0.0);
        voRepayReq.setIsUserOpen(false);
        try {
            repaymentBiz.repay(voRepayReq);
        } catch (Throwable e) {
            e.printStackTrace();
        }*/

        /*BidApplyQueryReq request = new BidApplyQueryReq();
        request.setAccountId("6212462040000600025");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_1498530231199");
        BidApplyQueryResp response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResp.class);
        System.out.println(response);*/


        /*Borrow borrow = borrowService.findById(169767L);
        try {
            borrowBiz.notTransferedBorrowAgainVerify(borrow);
        } catch (Throwable e) {
            e.printStackTrace();
        }*/

        /*Borrow borrow = borrowService.findById(165227L);
        try {
            borrowBiz.transferedBorrowAgainVerify(borrow);
        } catch (Throwable e) {
            e.printStackTrace();
        }*/
    }

}
