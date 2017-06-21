package com.gofobao.framework.award.biz;

import com.gofobao.framework.award.vo.VoViewCouponWarpRes;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/7.
 */
public interface CouponBiz {

    ResponseEntity<VoViewCouponWarpRes> list(VoCouponReq voCouponReq);

    /**
     * 流量券兑换
     * @param userId
     * @param couponId
     * @return
     */
    ResponseEntity<VoBaseResp>exchange(Long userId, Long couponId);



    /**
     * 流量兑换回调
     * @param key
     * @return
     * @throws Exception
     */
    String takeFlowCallBack(String key) throws Exception ;
}
