package com.gofobao.framework.award.controller;

import com.gofobao.framework.award.biz.CouponBiz;
import com.gofobao.framework.award.vo.VoViewCouponWarpRes;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/6/7.
 */
@RequestMapping("/coupon")
@RestController
@Api(description="流量券")
public class CouponController {

    @Autowired
    private CouponBiz couponBiz;


    @ApiOperation("流量券列表")
    @GetMapping("/v2/list/{status}/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewCouponWarpRes> list(@PathVariable Integer status,
                                                    @PathVariable Integer pageIndex,
                                                    @PathVariable Integer pageSize,
                                                    @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        VoCouponReq voCouponReq = new VoCouponReq();
        voCouponReq.setStatus(status);
        voCouponReq.setUserId(userId);
        voCouponReq.setPageIndex(pageIndex);
        voCouponReq.setPageSize(pageSize);
        return couponBiz.list(voCouponReq);
    }


}
