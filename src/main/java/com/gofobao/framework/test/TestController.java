package com.gofobao.framework.test;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/6/21.
 */
@RestController
@Api(description = "测试")
@RequestMapping
@Slf4j
public class TestController {
    @Autowired
    MqHelper mqHelper;
    final Gson GSON = new GsonBuilder().create();
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;

    @RequestMapping("/test/pub/batch/deal/{sourceId}/{batchNo}")
    public void batchDeal(@PathVariable("sourceId") String sourceId, @PathVariable("batchNo") String batchNo) {
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("batchNo", batchNo)
                .eq("sourceId", sourceId)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
        mqConfig.setTag(MqTagEnum.BATCH_DEAL);

        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.SOURCE_ID, sourceId,
                        MqConfig.BATCH_NO, batchNo,
                        MqConfig.MSG_TIME, DateHelper.dateToString(new Date()),
                        MqConfig.ACQ_RES, thirdBatchLogList.get(0).getAcqRes()
                );

        mqConfig.setMsg(body);
        try {
            log.info(String.format("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq %s", GSON.toJson(body)));
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            log.error("tenderThirdBizImpl thirdBatchRepayAllRunCall send mq exception", e);
        }
    }
}
