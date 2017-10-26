package com.gofobao.framework;

import com.gofobao.framework.as.bix.RechargeStatementBiz;
import com.gofobao.framework.as.bix.impl.RechargeStatementBizImpl;
import com.gofobao.framework.helper.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class StatementTest {

    @Autowired
    private RechargeStatementBiz rechargeStatementBiz;


    @Test
    public void testOfflineRecharge() {
        Long userId = 1699L;
        String statementDate = "2017-09-21 00:00:00";
        Date date = DateHelper.stringToDate(statementDate);
        try {
            boolean result = rechargeStatementBiz.offlineStatement(userId,
                    date,
                    RechargeStatementBizImpl.RechargeType.offlineRecharge);

            if (result) {
                log.info("同步成功");
            } else {
                log.error("同步失败");
            }
        } catch (Exception e) {
            log.error("实时在线同步", e);
        }
    }
}
