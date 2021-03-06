package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_query.BatchQueryReq;
import com.gofobao.framework.api.model.batch_query.BatchQueryResp;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/7/24.
 */
@Slf4j
@Component
public class DealThirdBatchScheduler {

    final Gson GSON = new GsonBuilder().create();

    @Autowired
    MqHelper mqHelper;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;

    @Scheduled(cron = "0 0/30 8,9,10,11,12,13,14,15,16,17,18,19,20,21,22 * * ? ")
    public void process() {
        log.info("处理第三方批次任务调度启动");
        //1.查询未处理 参数校验成功的批次 gfb_third_batch_log
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("state", 0, 1)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = null;
        int pageSize = 50;
        int index = 0;
        do {
            thirdBatchLogList = thirdBatchLogService.findList(tbls, new PageRequest(index, pageSize, new Sort(Sort.Direction.ASC, "id")));
            index++;
            thirdBatchLogList.forEach(thirdBatchLog -> {
                BatchQueryReq req = new BatchQueryReq();
                req.setChannel(ChannelContant.HTML);
                req.setBatchNo(thirdBatchLog.getBatchNo());
                String txDate = thirdBatchLog.getTxDate();
                req.setBatchTxDate(ObjectUtils.isEmpty(txDate) ? DateHelper.dateToString(thirdBatchLog.getCreateAt(), DateHelper.DATE_FORMAT_YMD_NUM) : txDate);
                BatchQueryResp resp = jixinManager.send(JixinTxCodeEnum.BATCH_QUERY, req, BatchQueryResp.class);
                if ((!ObjectUtils.isEmpty(resp))
                        && JixinResultContants.SUCCESS.equals(resp.getRetCode())
                        && "S".equals(resp.getBatchState())) {
                    //推送批次处理到队列中
                    MqConfig mqConfig = new MqConfig();
                    mqConfig.setQueue(MqQueueEnum.RABBITMQ_THIRD_BATCH);
                    mqConfig.setTag(MqTagEnum.BATCH_DEAL);
                    ImmutableMap<String, String> body = new ImmutableMap.Builder<String, String>()
                            .put(MqConfig.SOURCE_ID, StringHelper.toString(thirdBatchLog.getSourceId()))
                            .put(MqConfig.BATCH_NO, thirdBatchLog.getBatchNo())
                            .put(MqConfig.BATCH_TYPE, String.valueOf(thirdBatchLog.getType()))
                            .put(MqConfig.MSG_TIME, DateHelper.dateToString(new Date()))
                            .put(MqConfig.ACQ_RES, thirdBatchLog.getAcqRes())
                            .put(MqConfig.BATCH_RESP, "")
                            .build();

                    mqConfig.setMsg(body);
                    try {
                        log.info(String.format("DealThirdBatchScheduler process send mq %s", GSON.toJson(body)));
                        mqHelper.convertAndSend(mqConfig);
                    } catch (Throwable e) {
                        log.error("DealThirdBatchScheduler process send mq exception", e);
                    }

                }
            });
        } while (thirdBatchLogList.size() >= pageSize);
        log.info("################处理第三方批次任务调度批次调度结束####################");
    }
}
