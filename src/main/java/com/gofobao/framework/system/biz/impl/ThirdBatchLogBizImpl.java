package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.repository.ThirdBatchLogRepository;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/7/14.
 */
@Service
public class ThirdBatchLogBizImpl implements ThirdBatchLogBiz{

    @Autowired
    private ThirdBatchLogService thirdBatchLogService;

    /**
     * 更新批次日志状态
     * @param batchNo
     * @return
     */
    public boolean updateBatchLogState(String batchNo,Long sourceId){
        ThirdBatchLog thirdBatchLog = thirdBatchLogService.findByBatchNoAndSourceId(batchNo,sourceId);
        if (ObjectUtils.isEmpty(thirdBatchLog)){
            return false;
        }
        thirdBatchLog.setState(true);
        thirdBatchLogService.save(thirdBatchLog);
        return true;
    }
}
