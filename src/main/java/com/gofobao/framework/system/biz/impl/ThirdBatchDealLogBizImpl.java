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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
                .in("state", 0, 1)
                .build();
        List<ThirdBatchLog> thirdBatchLogList = thirdBatchLogService.findList(tbls);

        return ResponseEntity.ok(VoBaseResp.ok("查询成功!"));
    }
}

