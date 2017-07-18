package com.gofobao.framework.system.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_query.BatchQueryReq;
import com.gofobao.framework.api.model.batch_query.BatchQueryResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.system.biz.ThirdBatchLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchNoStateContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.repository.ThirdBatchLogRepository;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/7/14.
 */
@Service
@Slf4j
public class ThirdBatchLogBizImpl implements ThirdBatchLogBiz {

    @Autowired
    private ThirdBatchLogService thirdBatchLogService;
    @Autowired
    private JixinManager jixinManager;

    /**
     * 更新批次日志状态
     *
     * @param batchNo
     * @return
     */
    public boolean updateBatchLogState(String batchNo, Long sourceId, int state) {
        ThirdBatchLog thirdBatchLog = thirdBatchLogService.findByBatchNoAndSourceId(batchNo, sourceId);
        if (ObjectUtils.isEmpty(thirdBatchLog)) {
            return false;
        }
        thirdBatchLog.setState(state);
        thirdBatchLogService.save(thirdBatchLog);
        return true;
    }

    /**
     * 更据sourceId检查批次是否频繁提交
     *
     * @param sourceId
     * @return
     */
    public boolean checkBatchOftenSubmit(String sourceId, int ... type) {
        //查询最后一条提交的批次
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", sourceId)
                .in("state", 0, 1)
                .eq("type", type)
                .build();
        Pageable pageable = new PageRequest(0, 1, new Sort(Sort.Direction.DESC, "id"));
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls, pageable);
        if (CollectionUtils.isEmpty(thirdBatchLogList)) {
            return false;
        }

        //判断这个批次是否处理成功
        ThirdBatchLog thirdBatchLog = thirdBatchLogList.get(0);
        BatchQueryReq req = new BatchQueryReq();
        req.setChannel(ChannelContant.HTML);
        req.setBatchNo(thirdBatchLog.getBatchNo());
        req.setBatchTxDate(DateHelper.dateToString(thirdBatchLog.getCreateAt(), DateHelper.DATE_FORMAT_YMD_NUM));
        BatchQueryResp resp = jixinManager.send(JixinTxCodeEnum.BATCH_QUERY, req, BatchQueryResp.class);
        if ((ObjectUtils.isEmpty(resp)) || (!JixinResultContants.SUCCESS.equals(resp.getRetCode()))) {
            log.error(ObjectUtils.isEmpty(resp) ? "当前网络不稳定，请稍候重试" : resp.getRetMsg());
            return true;
        }

        if (resp.getBatchState().equals(ThirdBatchNoStateContants.DISPOSING)) {
            return true;
        }

        return false;
    }
}
