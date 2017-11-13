package com.gofobao.framework.listener.providers;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.system.biz.ThirdBatchDealBiz;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Zeke on 2017/7/18.
 */
@Component
@Slf4j
public class ThirdBatchProvider {

    @Autowired
    private ThirdBatchDealBiz thirdBatchDealBiz;

    public boolean batchDeal(Map<String, String> msg) {
        String sourceId = msg.get(MqConfig.SOURCE_ID);
        String batchNo = msg.get(MqConfig.BATCH_NO);
        String batchType = msg.get(MqConfig.BATCH_TYPE);
        String acqRes = msg.get(MqConfig.ACQ_RES);
        String batchResp = msg.get(MqConfig.BATCH_RESP);
        if (StringUtils.isEmpty(sourceId)
                || StringUtils.isEmpty(batchNo)
                || StringUtils.isEmpty(batchType)) {
            log.error(String.format("批次执行缺少必填参数，sourceId->%s,batchNo->%s,batchType->%s", sourceId, batchNo, batchType));
            return false;
        }

        try {
            thirdBatchDealBiz.batchDeal(NumberHelper.toLong(sourceId), batchNo, NumberHelper.toInt(batchType), acqRes, batchResp);
        } catch (Exception e) {
            log.error("批次执行异常:", e);
            return false;
        }
        return true;
    }
}
