package com.gofobao.framework;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
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
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.VoAdvanceCall;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void contextLoads() {
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_AUTO_TENDER);
        mqConfig.setTag(MqTagEnum.AUTO_TENDER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_BORROW_ID, StringHelper.toString("169782"), MqConfig.MSG_TIME, DateHelper.dateToString(new Date()));
        mqConfig.setMsg(body);
        try {
            mqHelper.convertAndSend(mqConfig);
        } catch (Exception e) {
            log.error("borrowProvider autoTender send mq exception", e);
        }
    }

    public static void main(String[] args) {

        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();
        map.put("repaymentId", "173796");
        System.out.println(gson.toJson(map));
        System.out.println(SecurityHelper.getSign(gson.toJson(map)));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {


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
        advanceCall();

        /*Map<String,String> map = new HashMap<>();
        map.put("borrowId","169761");
        try {
            borrowProvider.doFirstVerify(map);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setRepaymentId(168683L);
        voRepayReq.setUserId(901L);
        voRepayReq.setIsUserOpen(false);
        voRepayReq.setInterestPercent(1d);
        try {
            repaymentBiz.repay(voRepayReq);
        } catch (Exception e) {
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*BatchDetailsQueryReq request = new BatchDetailsQueryReq();
        request.setBatchNo("103146");
        request.setBatchTxDate("20170705");
        request.setType("1");
        request.setPageNum("1");
        request.setPageSize("10");
        request.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp response = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, request, BatchDetailsQueryResp.class);
        System.out.println(response);*/

        /*BidApplyQueryReq request = new BidApplyQueryReq();
        request.setAccountId("6212462040000600025");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_1498530231199");
        BidApplyQueryResp response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResp.class);
        System.out.println(response);*/


        /*Borrow borrow = borrowService.findById(169767L);
        try {
            borrowBiz.notTransferedBorrowAgainVerify(borrow);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*Borrow borrow = borrowService.findById(165227L);
        try {
            borrowBiz.transferedBorrowAgainVerify(borrow);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
