package com.gofobao.framework;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.account_query_by_mobile.AccountQueryByMobileRequest;
import com.gofobao.framework.api.model.account_query_by_mobile.AccountQueryByMobileResponse;
import com.gofobao.framework.api.model.batch_bail_repay.BailRepayRun;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryReq;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResp;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryRequest;
import com.gofobao.framework.api.model.credit_auth_query.CreditAuthQueryResponse;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryReq;
import com.gofobao.framework.api.model.credit_invest_query.CreditInvestQueryResp;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResp;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryReq;
import com.gofobao.framework.api.model.trustee_pay_query.TrusteePayQueryResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoQueryThirdBorrowList;
import com.gofobao.framework.common.assets.AssetsChangeEntity;
import com.gofobao.framework.common.assets.AssetsChangeEnum;
import com.gofobao.framework.common.assets.AssetsChangeHelper;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.VoAdvanceCall;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
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
import org.springframework.util.ObjectUtils;

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
    private AssetsChangeHelper assetsChangeHelper;


    @Test
    public void testAssetsChange() throws Exception {

        AssetsChangeEntity freeze = new AssetsChangeEntity();
        freeze.setMoney(100 * 100 - 2 * 100);
        freeze.setFee(2 * 100);
        freeze.setType(AssetsChangeEnum.Frozen);
        freeze.setUserId(41258);
        freeze.setToUserId(0);
        freeze.setRefId(12);
        assetsChangeHelper.execute(freeze);
        AssetsChangeEntity cash = new AssetsChangeEntity();
        cash.setMoney(100 * 100 - 2 * 100);
        cash.setFee(2 * 100);
        cash.setType(AssetsChangeEnum.SmallCash);
        cash.setUserId(41258);
        cash.setToUserId(0);
        cash.setRefId(12);
        assetsChangeHelper.execute(cash);
    }


    @Test
    public void contextLoads() throws InterruptedException {
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_AUTO_TENDER);
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
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();
        map.put("repaymentId", "173810");
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void doFirstVerify() {
        Map<String, String> map = new HashMap<>();
        map.put("borrowId", "169850");
        try {
            borrowProvider.doFirstVerify(map);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void repayDeal() {
        VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setRepaymentId(168683L);
        voRepayReq.setUserId(901L);
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
        voQueryThirdBorrowList.setProductId("A69756");
        voQueryThirdBorrowList.setUserId(2762L);
        voQueryThirdBorrowList.setPageNum("1");
        voQueryThirdBorrowList.setPageSize("10");
        DebtDetailsQueryResp resp = borrowThirdBiz.queryThirdBorrowList(voQueryThirdBorrowList);
        System.out.println((resp.getTotalItems()));


    }

    private void doAgainVerify() {
        Map<String, String> msg = new HashMap<>();
        msg.put("borrowId", "169820");
        try {
            borrowProvider.doAgainVerify(msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void batchDetailsQuery() {
        BatchDetailsQueryReq batchDetailsQueryReq = new BatchDetailsQueryReq();
        batchDetailsQueryReq.setBatchNo("093859");
        batchDetailsQueryReq.setBatchTxDate("20170711");
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
        request.setAccountId("6212462040000600025");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_1498530231199");
        BidApplyQueryResp response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResp.class);
        System.out.println(response);

    }

    private void transferBorrowAgainVerify() {
        Borrow borrow = borrowService.findById(165227L);
        try {
            borrowBiz.transferBorrowAgainVerify(borrow);
        } catch (Throwable e) {
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
        //advanceCall();
        //初审
        doFirstVerify();
        //还款处理
        //repayDeal();
        //查询标的集合
        //findThirdBorrowList();
        //复审
        //doAgainVerify();
        //批次详情查询
        //batchDetailsQuery();
        //查询投标申请
        //bidApplyQuery();
        //转让标复审回调
        //transferBorrowAgainVerify();
    }

}
