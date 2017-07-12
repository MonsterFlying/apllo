package com.gofobao.framework.award.controller.web;

import com.gofobao.framework.award.biz.VirtualBiz;
import com.gofobao.framework.award.vo.request.VoVirtualReq;
import com.gofobao.framework.award.vo.response.VoViewVirtualBorrowResWarpRes;
import com.gofobao.framework.award.vo.response.VoViewVirtualStatisticsWarpRes;
import com.gofobao.framework.award.vo.response.VoViewVirtualTenderResWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/8.
 */

@RestController
@Api(description = "pc:体验金")
public class WebVirtualController {

    @Autowired
    private VirtualBiz virtualBiz;

    @ApiOperation("pc:收益统计")
    @PostMapping(value = "virtual/v2/statistics")
    public ResponseEntity<VoViewVirtualStatisticsWarpRes> query(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        return virtualBiz.query(userId);
    }

    @ApiOperation("pc:用户投标体验金列表")
    @PostMapping(value = "virtual/v2/userTenderList")
    public ResponseEntity<VoViewVirtualTenderResWarpRes> userTenderList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        return virtualBiz.userTenderList(userId);
    }

    @ApiOperation("pc:体验金列表")
    @PostMapping(value = "virtual/v2/list")
    public ResponseEntity<VoViewVirtualBorrowResWarpRes> list() {
        return virtualBiz.list();
    }

    @ApiOperation("pc:体验金投标")
    @PostMapping("virtual/v2/createTender")
    public ResponseEntity<VoBaseResp> createTender(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                   @ModelAttribute VoVirtualReq voVirtualReq) {
        voVirtualReq.setUserId(userId);
        return virtualBiz.createTender(voVirtualReq);
    }




}
