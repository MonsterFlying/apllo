package com.gofobao.framework.system.biz;

import com.gofobao.framework.system.vo.request.VoFindLendRepayStatusListReq;
import com.gofobao.framework.system.vo.response.VoViewFindLendRepayStatusListRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/9/12.
 */
public interface ThirdBatchDealLogBiz {
    ResponseEntity<VoViewFindLendRepayStatusListRes> findLendRepayStatusList(VoFindLendRepayStatusListReq voFindLendRepayStatusListReq);
}
