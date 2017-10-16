package com.gofobao.framework.system.biz;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.system.entity.ThirdBatchDealLog;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.vo.request.VoFindLendRepayStatusListReq;
import com.gofobao.framework.system.vo.request.VoFindRepayStatusListReq;
import com.gofobao.framework.system.vo.response.VoFindLendRepayStatus;
import com.gofobao.framework.system.vo.response.VoFindRepayStatus;
import com.gofobao.framework.system.vo.response.VoViewFindLendRepayStatusListRes;
import com.gofobao.framework.system.vo.response.VoViewFindRepayStatusListRes;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/9/12.
 */
public interface ThirdBatchDealLogBiz {

    List<VoFindRepayStatus> getVoFindRepayStatusList(Long collectionId, Long repaymentId);

    /**
     * 查询放款状态集合
     *
     * @param voFindLendRepayStatusListReq
     * @return
     */
    ResponseEntity<VoViewFindLendRepayStatusListRes> findLendRepayStatusList(VoFindLendRepayStatusListReq voFindLendRepayStatusListReq);

    /**
     * 查询还款状态集合
     *
     * @param voFindRepayStatusListReq
     * @return
     */
    ResponseEntity<VoViewFindRepayStatusListRes> findRepayStatusList(VoFindRepayStatusListReq voFindRepayStatusListReq);

    /**
     * 记录批次执行记录
     *
     * @param state
     * @param type
     * @return
     */
    ThirdBatchDealLog recordThirdBatchDealLog(String batchNo, long sourceId, int state, boolean status, int type, String errorMsg);
}
