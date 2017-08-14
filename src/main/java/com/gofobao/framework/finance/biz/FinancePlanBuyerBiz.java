package com.gofobao.framework.finance.biz;

import com.gofobao.framework.finance.vo.response.PlanBuyUserListWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/8/14.
 */
public interface FinancePlanBuyerBiz {


    ResponseEntity<PlanBuyUserListWarpRes> buyUserList(Long id);
}
