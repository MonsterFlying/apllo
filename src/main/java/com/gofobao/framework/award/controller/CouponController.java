package com.gofobao.framework.award.controller;

import com.gofobao.framework.award.biz.CouponBiz;
import com.gofobao.framework.award.vo.request.VoCouponReq;
import com.gofobao.framework.award.vo.request.VoTakeFlowReq;
import com.gofobao.framework.award.vo.response.VoViewCouponWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 2017/6/7.
 */

@RestController
@Api(description = "流量券")
@Slf4j
public class CouponController {

    @Autowired
    private CouponBiz couponBiz;

    @ApiOperation("流量券列表;status状态  0:无效； 1:有效")
    @GetMapping("coupon/v2/list/{status}/{pageIndex}/{pageSize}")
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

    @ApiOperation("流量劵兑换")
    @PostMapping("coupon/v2/takeFlow")
    public ResponseEntity<VoBaseResp> takeFlow(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                               @ModelAttribute VoTakeFlowReq takeFlowReq) {
        takeFlowReq.setUserId(userId);
        return couponBiz.exchange(takeFlowReq);
    }

    @PostMapping("pub/coupon/v2/takeFlowBackCall")
    public void takeFlowBackCall(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
        Iterator<Map.Entry<String, String[]>> iterator = entries.iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String[]> next = iterator.next();
            String key = next.getKey();
            String[] values = next.getValue();
            if (ObjectUtils.isEmpty(key)) {
                continue;
            }

            StringBuffer value = new StringBuffer();
            if (!ArrayUtils.isEmpty(values)) {
                for (String bean : values) {
                    value.append(bean);
                }
            }

            try {
                String result = couponBiz.takeFlowCallBack(key + value.toString());
                try (PrintWriter writer = response.getWriter()) {
                    writer.write(result);
                    writer.flush();
                    writer.close();
                }
            } catch (Throwable e) {
                log.error("流量对调错误", e);
            }
        }
    }

}
