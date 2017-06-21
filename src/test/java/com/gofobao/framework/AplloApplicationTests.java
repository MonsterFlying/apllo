package com.gofobao.framework;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.api.model.batch_repay_bail.BatchRepayBailResp;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryReq;
import com.gofobao.framework.api.model.bid_apply_query.BidApplyQueryResp;
import com.gofobao.framework.api.model.bid_auto_apply.BidAutoApplyRequest;
import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.borrow.vo.request.VoQueryThirdBorrowList;
import com.gofobao.framework.borrow.vo.request.VoRepayAllReq;
import com.gofobao.framework.common.integral.IntegralChangeEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.IntegralChangeHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.service.impl.LoanServiceImpl;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class AplloApplicationTests {

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

    @Test
    public void contextLoads() {
        Borrow borrow = borrowService.findById(165176L);
        try {
            borrowBiz.notTransferedBorrowAgainVerify(borrow);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("borrowId",165205);

        Gson gson = new Gson();
        String paramStr = gson.toJson(map);
        System.out.println(paramStr);

        System.out.println(SecurityHelper.getSign(paramStr));
    }

    @Test
    public void test() {


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
        voQueryThirdBorrowList.setBorrowId(165200L);
        voQueryThirdBorrowList.setUserId(901L);
        voQueryThirdBorrowList.setPageNum("1");
        voQueryThirdBorrowList.setPageSize("10");
        DebtDetailsQueryResp resp = borrowThirdBiz.queryThirdBorrowList(voQueryThirdBorrowList);
        System.out.println((resp.getTotalItems()));*/

       /* Map<String,String> msg = new HashMap<>();
        msg.put("borrowId","165184");
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


        /*BidApplyQueryReq request = new BidApplyQueryReq();
        request.setAccountId("6212462040000100018");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_1497494278131");
        BidApplyQueryResp response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResp.class);
        System.out.println(response);*/

        /*BatchDetailsQueryReq request = new BatchDetailsQueryReq();
        request.setBatchNo("100002");
        request.setBatchTxDate("20170619");
        request.setType("1");
        request.setPageNum("1");
        request.setPageSize("10");
        request.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp response = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, request, BatchDetailsQueryResp.class);
        System.out.println(response);*/

        /*Borrow borrow = borrowService.findById(165198L);
        try {
            borrowBiz.notTransferedBorrowAgainVerify(borrow);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
