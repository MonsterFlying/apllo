package com.gofobao.framework.award.biz;

import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.award.vo.request.VoTakeFlowReq;
import com.gofobao.framework.award.vo.response.CouponTackeRes;
import com.gofobao.framework.award.vo.response.VoViewCouponWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/7.
 */
public interface CouponBiz {

    ResponseEntity<VoViewCouponWarpRes> list(VoCouponReq voCouponReq);

    /**
     * pc流量券列表
     * @param voCouponReq
     * @return
     */
    ResponseEntity<VoViewCouponWarpRes> pcList(VoCouponReq voCouponReq);

    /**
     * 流量券兑换
     *takeFlowReq
     * @return
     */
    ResponseEntity<CouponTackeRes> exchange(VoTakeFlowReq takeFlowReq);



    /**
     * 流量兑换回调
     * @param key
     * @return
     * @throws Exception
     */
    String takeFlowCallBack(String key) throws Exception ;


}
