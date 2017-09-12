package com.gofobao.framework.system.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.system.vo.request.VoFindLendRepayStatusList;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Zeke on 2017/9/12.
 */
@Service
public class ThirdBatchDealLogBizImpl implements ThirdBatchDealLogBiz {

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;

    public ResponseEntity<VoBaseResp> findLendRepayStatusList(VoFindLendRepayStatusList voFindLendRepayStatusList) {
        /*借款id*/
        long borrowId = voFindLendRepayStatusList.getBorrowId();
        /*借款对象*/
        Borrow borrow = borrowService.findById(borrowId);
        /*查询批次*/
        Specification<ThirdBatchLog> tbls = Specifications
                .<ThirdBatchLog>and()
                .eq("sourceId", borrow.getId())
                .eq("type", ThirdBatchLogContants.BATCH_LEND_REPAY)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls, new Sort(Sort.Direction.DESC, "createdAt"));
        /* 批次处理记录 */
        ThirdBatchLog thirdBatchLog = null;
        if (!CollectionUtils.isEmpty(thirdBatchLogList)) {
            //已完成批次
            List<ThirdBatchLog> successThirdBatchLogList = thirdBatchLogList.stream().filter(t -> t.getType().intValue() == 3).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(successThirdBatchLogList)) {
                thirdBatchLog = successThirdBatchLogList.get(1);
            } else {
                thirdBatchLog = thirdBatchLogList.get(0);
            }
        }
        //不存在已完成批次，继续获取批次处理记录

        return ResponseEntity.ok(VoBaseResp.ok("查询成功!"));
    }
}

