package com.gofobao.framework.award.controller.web;

import com.gofobao.framework.award.biz.CouponBiz;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.award.vo.request.VoTakeFlowReq;
import com.gofobao.framework.award.vo.response.CouponTackeRes;
import com.gofobao.framework.award.vo.response.VoViewCouponWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/6/7.
 */

@RestController
@Api(description = "pc:流量券")
@Slf4j
public class WebCouponController {

    @Autowired
    private CouponBiz couponBiz;

    @ApiOperation("流量券列表;status状态 （1有效;2锁定中;3已用;4失效）")
    @GetMapping("coupon/pc/v2/list/{status}/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewCouponWarpRes> list(@PathVariable Integer status,
                                                    @PathVariable Integer pageIndex,
                                                    @PathVariable Integer pageSize,
                                                    @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCouponReq voCouponReq = new VoCouponReq();
        voCouponReq.setStatus(status);
        voCouponReq.setUserId(userId);
        voCouponReq.setPageIndex(pageIndex);
        voCouponReq.setPageSize(pageSize);
        return couponBiz.pcList(voCouponReq);
    }

    @ApiOperation("流量劵兑换")
    @PostMapping("coupon/pc/v2/takeFlow")
    public ResponseEntity<CouponTackeRes> takeFlow(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                   @ModelAttribute VoTakeFlowReq takeFlowReq) {
        takeFlowReq.setUserId(userId);
        return couponBiz.exchange(takeFlowReq);
    }


}

