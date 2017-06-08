package com.gofobao.framework;

import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.common.integral.IntegralChangeEntity;

import java.util.HashMap;
import java.util.Map;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.helper.project.IntegralChangeHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.gofobao.framework.repayment.service.impl.LoanServiceImpl;
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
    MqHelper mqHelper ;


    @Autowired
    private LoanServiceImpl loanService;
    @Autowired
    private BorrowThirdBiz borrowThirdBiz;
    @Autowired
    private IntegralChangeHelper integralChangeHelper;
    @Autowired
    private BorrowProvider borrowProvider;

    @Test
    public void contextLoads() {
    }

    @Test
    public void test(){

        Map<String,String> map = new HashMap();
        map.put("borrowId","165153");
        try {
            borrowProvider.doAgainVerify(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
