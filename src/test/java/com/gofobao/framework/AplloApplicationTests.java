package com.gofobao.framework;

import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import com.gofobao.framework.common.integral.IntegralChangeEntity;
import com.gofobao.framework.common.integral.IntegralChangeEnum;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.helper.project.IntegralChangeHelper;
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

    @Test
    public void contextLoads() {
    }

    @Test
    public void test(){

        IntegralChangeEntity entity = new IntegralChangeEntity();
        entity.setUserId(901L);
        entity.setValue(1000);
        entity.setType(IntegralChangeEnum.TENDER);
        try {
            integralChangeHelper.integralChange(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
