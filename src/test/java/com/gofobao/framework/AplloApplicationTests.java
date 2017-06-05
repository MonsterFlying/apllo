package com.gofobao.framework;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.service.impl.LoanServiceImpl;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.VoViewBudingRes;
import com.gofobao.framework.repayment.vo.response.VoViewRefundRes;
import com.gofobao.framework.repayment.vo.response.VoViewSettleRes;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class AplloApplicationTests {

    @Autowired
    MqHelper mqHelper ;


    @Autowired
    private LoanServiceImpl loanService;

    @Test
    public void contextLoads() {
        MqConfig mqConfig = new MqConfig() ;
        mqConfig.setTag(MqTagEnum.SMS_REGISTER);
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_SMS);
        ImmutableMap<String, String> body = ImmutableMap.of("i", "qqqq") ;
        mqConfig.setMsg(body);
        mqConfig.setSendTime(DateHelper.addMinutes(new Date(), 3));
        mqHelper.convertAndSend(mqConfig) ;
    }

    @Test
    public void test(){
        VoLoanListReq voLoanListReq=new VoLoanListReq();
        voLoanListReq.setUserId(901L);
        voLoanListReq.setPageIndex(1);
        voLoanListReq.setPageSize(10);
        List<VoViewSettleRes> refundResList= loanService.settleList(voLoanListReq);

        refundResList.stream().forEach(p->System.out.println(p.getBorrowName()));

    }

}
