package com.gofobao.framework.award.service;

import com.gofobao.framework.award.entity.Coupon;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.award.vo.response.CouponRes;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
public interface CouponService {

    List<CouponRes>list(VoCouponReq couponReq);

    /**
     * 流量券兑换
     * @param userId
     * @param couponId
     * @return
     */
    List<Coupon> takeFlow(Long userId, Long couponId);


    Coupon save(Coupon coupon);
}
