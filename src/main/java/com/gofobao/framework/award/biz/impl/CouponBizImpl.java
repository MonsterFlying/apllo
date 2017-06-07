package com.gofobao.framework.award.biz.impl;

import com.gofobao.framework.award.biz.CouponBiz;
import com.gofobao.framework.award.service.CouponService;
import com.gofobao.framework.award.vo.VoViewCouponWarpRes;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.award.vo.response.CouponRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Slf4j
@Service
public class CouponBizImpl implements CouponBiz {
    @Autowired
    private CouponService couponService;

    @Override
    public ResponseEntity<VoViewCouponWarpRes> list(VoCouponReq voCouponReq) {
        try {
            List<CouponRes> resList = couponService.list(voCouponReq);
            VoViewCouponWarpRes voViewCouponWarpRes = VoBaseResp.ok("查询成功", VoViewCouponWarpRes.class);
            voViewCouponWarpRes.setCouponList(resList);
            return ResponseEntity.ok(voViewCouponWarpRes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp
                            .error(VoBaseResp.ERROR, "查询失败", VoViewCouponWarpRes.class));
        }
    }
}
