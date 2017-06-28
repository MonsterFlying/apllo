package com.gofobao.framework.integral.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.integral.vo.request.VoIntegralTakeReq;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import com.gofobao.framework.integral.vo.response.VoListIntegralResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface IntegralBiz {

    /**
     * 获取积分列表
     *
     * @param voListIntegralReq
     * @return
     */
    ResponseEntity<VoListIntegralResp> list(VoListIntegralReq voListIntegralReq);

    /**
     * 积分兑换
     *
     * @param voIntegralTakeReq
     * @return
     */
    ResponseEntity<VoBaseResp> doTakeRates(VoIntegralTakeReq voIntegralTakeReq) throws Exception;


}
