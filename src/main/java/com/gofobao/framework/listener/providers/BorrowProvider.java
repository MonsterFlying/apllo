package com.gofobao.framework.listener.providers;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/31.
 */
@Component
@Slf4j
public class BorrowProvider {

    Gson GSON = new GsonBuilder().create();

    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    /**
     * 正常放款流程(禁止流转标调用此方法)
     *
     * @param msg
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean doAgainVerify(Map<String, String> msg) throws Exception {
        Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get("borrowId")));
        Borrow borrow = borrowService.findByIdLock(borrowId);
        if (borrow.getStatus() != 1) {
            log.error("复审：借款状态已发生改变！");
            return false;
        }

        log.info(String.format("复审: 批量正常放款申请开始: %s", GSON.toJson(msg)));

        //批次放款
        VoThirdBatchLendRepay voThirdBatchLendRepay = new VoThirdBatchLendRepay();
        voThirdBatchLendRepay.setBorrowId(borrowId);
        ResponseEntity<VoBaseResp> resp = borrowRepaymentThirdBiz.thirdBatchLendRepay(voThirdBatchLendRepay);

        String data = GSON.toJson(msg);
        if (resp.getBody().getState().getCode() == VoBaseResp.OK) {
            log.info(String.format("复审: 批量正常放款申请申请成功: %s", data));
            return true;
        } else {
            exceptionEmailHelper.sendErrorMessage("放款失败", data);
            log.info(String.format("复审: 批量正常放款申请申请失败: %s", data));
            return false;
        }

    }
}
