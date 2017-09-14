package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.entity.ThirdBatchDealLog;
import com.gofobao.framework.system.vo.request.VoFindLendRepayStatusListReq;
import com.gofobao.framework.system.vo.response.VoViewFindLendRepayStatusListRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/9/12.
 */
public interface ThirdBatchDealLogBiz {

    /**
     * 查询放款状态集合
     *
     * @param voFindLendRepayStatusListReq
     * @return
     */
    ResponseEntity<VoViewFindLendRepayStatusListRes> findLendRepayStatusList(VoFindLendRepayStatusListReq voFindLendRepayStatusListReq);

    /**
     * 记录批次执行记录
     * @param state
     * @param type
     * @return
     */
    ThirdBatchDealLog recordThirdBatchDealLog(String batchNo,long sourceId ,int state, boolean status ,int type, String errorMsg);
}
