package com.gofobao.framework.award.controller;

import com.gofobao.framework.award.biz.VirtualBiz;
import com.gofobao.framework.award.vo.request.VoVirtualReq;
import com.gofobao.framework.award.vo.response.VoViewVirtualBorrowResWarpRes;
import com.gofobao.framework.award.vo.response.VoViewVirtualStatisticsWarpRes;
import com.gofobao.framework.award.vo.response.VoViewVirtualTenderResWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/8.
 */

@RestController
@RequestMapping(value = "/virtual")
@Api(description = "体验金")
public class VirtualController {

    @Autowired
    private VirtualBiz virtualBiz;

    @ApiOperation("收益统计")
    @PostMapping(value = "/v2/statistics")
    public ResponseEntity<VoViewVirtualStatisticsWarpRes> query(/*@RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
      Long userId=901L;
        return virtualBiz.query(userId);
    }

    @ApiOperation("用户投标体验金列表")
    @PostMapping(value = "/v2/userTenderList")
    public ResponseEntity<VoViewVirtualTenderResWarpRes> userTenderList(/*@RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        Long userId=901L;
        return virtualBiz.userTenderList(userId);
    }

    @ApiOperation("体验金列表")
    @PostMapping(value = "/v2/list")
    public ResponseEntity<VoViewVirtualBorrowResWarpRes> list() {
        return virtualBiz.list();
    }

    @ApiOperation("体验金投标")
    @PostMapping("v2/createTender")
    public ResponseEntity<VoBaseResp> createTender(@ModelAttribute VoVirtualReq voVirtualReq) {
        return virtualBiz.createTender(voVirtualReq);
    }

}
