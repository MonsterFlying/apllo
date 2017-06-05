package com.gofobao.framework;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.google.common.collect.ImmutableMap;
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

public class AplloApplicationTests {

    @Autowired
    MqHelper mqHelper ;

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

}
