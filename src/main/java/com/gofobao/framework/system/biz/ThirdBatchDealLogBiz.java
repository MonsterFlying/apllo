package com.gofobao.framework.system.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.vo.request.VoFindLendRepayStatusList;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/9/12.
 */
public interface ThirdBatchDealLogBiz {
    ResponseEntity<VoBaseResp> findLendRepayStatusList(VoFindLendRepayStatusList voFindLendRepayStatusList);
}
