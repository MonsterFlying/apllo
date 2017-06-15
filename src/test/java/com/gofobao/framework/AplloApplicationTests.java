package com.gofobao.framework;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryReq;
import com.gofobao.framework.api.model.batch_details_query.BatchDetailsQueryResp;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class AplloApplicationTests {

    @Autowired
    MqHelper mqHelper;

    @Autowired
    private BorrowProvider borrowProvider;
    @Autowired
    private JixinHelper jixinHelper;

    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private JixinManager jixinManager;

    @Test
    public void contextLoads() {
        Borrow borrow = borrowService.findById(165176L);
        try {
            borrowBiz.notTransferedBorrowAgainVerify(borrow);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {

        /*BidApplyQueryReq request = new BidApplyQueryReq();
        request.setAccountId("6212462040000100018");
        request.setChannel(ChannelContant.HTML);
        request.setOrgOrderId("GFBT_1497409988702");
        BidApplyQueryResp response = jixinManager.send(JixinTxCodeEnum.BID_APPLY_QUERY, request, BidApplyQueryResp.class);
        System.out.println(response);*/

        BatchDetailsQueryReq request = new BatchDetailsQueryReq();
        request.setBatchNo("100001");
        request.setBatchTxDate("20170614");
        request.setType("2");
        request.setPageNum("1");
        request.setPageSize("10");
        request.setChannel(ChannelContant.HTML);
        BatchDetailsQueryResp response = jixinManager.send(JixinTxCodeEnum.BATCH_DETAILS_QUERY, request, BatchDetailsQueryResp.class);
        System.out.println(response);
        /*Borrow borrow = borrowService.findById(165176L);
        try {
            borrowBiz.notTransferedBorrowAgainVerify(borrow);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
