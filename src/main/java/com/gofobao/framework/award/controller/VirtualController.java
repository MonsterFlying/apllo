package com.gofobao.framework.award.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/8.
 */

@RestController
@Api(description = "体验金")
public class VirtualController {

    /*@Autowired
    private VirtualBiz virtualBiz;

    @ApiOperation("收益统计")
    @PostMapping(value = "virtual/v2/statistics")
    public ResponseEntity<VoViewVirtualStatisticsWarpRes> query(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return virtualBiz.query(userId);
    }

    @ApiOperation("用户投标体验金列表")
    @PostMapping(value = "virtual/v2/userTenderList")
    public ResponseEntity<VoViewVirtualTenderResWarpRes> userTenderList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return virtualBiz.userTenderList(userId);
    }

    @ApiOperation("体验金列表")
    @PostMapping(value = "virtual/v2/list")
    public ResponseEntity<VoViewVirtualBorrowResWarpRes> list() {
        return virtualBiz.list();
    }

    @ApiOperation("体验金投标")
    @PostMapping("virtual/v2/createTender")
    public ResponseEntity<VoBaseResp> createTender(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                   @ModelAttribute VoVirtualReq voVirtualReq) {
        voVirtualReq.setUserId(userId);
        return virtualBiz.createTender(voVirtualReq);
    }
*/
}
