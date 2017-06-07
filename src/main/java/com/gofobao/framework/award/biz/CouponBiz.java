package com.gofobao.framework.award.biz;

import com.gofobao.framework.award.vo.VoViewCouponWarpRes;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/7.
 */
public interface CouponBiz {

    ResponseEntity<VoViewCouponWarpRes> list(VoCouponReq voCouponReq);
}
