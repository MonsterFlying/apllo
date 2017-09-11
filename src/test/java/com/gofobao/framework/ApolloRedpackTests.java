package com.gofobao.framework;

import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.tender.vo.request.VoPublishRedReq;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ApolloRedpackTests {

    @Autowired
    RedPackageBiz redPackageBiz;

    @Test
    public void testPublishRedpack() {
        VoPublishRedReq voPublishRedReq = new VoPublishRedReq();

        Map<String, String> data = new HashMap<>();
        data.put("beginTime", "2017-09-06 00:00:00");
        voPublishRedReq.setParamStr(new Gson().toJson(data));
        redPackageBiz.publishActivity(voPublishRedReq);

        try {
            Thread.sleep(60000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
