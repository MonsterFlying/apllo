package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/7/18.
 */
@Component
@Slf4j
public class ThirdBatchProvider {

    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private ThirdBatchLogBiz thirdBatchLogBiz;

    /**
     * 批次处理
     *
     * @param msg
     * @return
     */
    public boolean batchDeal(Map<String, Object> msg) {
        Long sourceId = NumberHelper.toLong(msg.get(MqConfig.SOURCE_ID));//batchLog sourceId
        Long batchNo = NumberHelper.toLong(msg.get(MqConfig.BATCH_NO));//batchLog batchNo

        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId",sourceId)
                .eq("batchNo",batchNo)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);
        if (CollectionUtils.isEmpty(thirdBatchLogList)){
            log.info("ThirdBatchProvider batchDeal:未查询到批次记录!");
            return false;
        }
        //主动查询未改变记录的批次状态，
        ThirdBatchLog thirdBatchLog = thirdBatchLogList.get(0);

        //批次存在失败批次，处理失败批次
        //不存在失败批次进行后续操作

        return false;
    }


}
