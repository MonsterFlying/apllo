package com.gofobao.framework.member.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.BrokerBounsBiz;
import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.request.VoFriendsTenderReq;
import com.gofobao.framework.member.vo.response.VoViewInviteAwardStatisticsWarpRes;
import com.gofobao.framework.member.vo.response.pc.VoViewBrokerBounsWarpRes;
import com.gofobao.framework.member.vo.response.pc.VoViewInviteFriendsWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by admin on 2017/7/4.
 */
@Slf4j
@RequestMapping("")
@Api(description = "pc:我的邀请")
@RestController
public class WebFriendsController {

    @Autowired
    private BrokerBounsBiz brokerBounsBiz;

    @ApiOperation("邀请好友列表,type:0全部，1：提成")
    @GetMapping("/invite/pc/v2/list/{type}/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewInviteFriendsWarpRes> list(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                           @PathVariable Integer type,
                                                           @PathVariable Integer pageIndex,
                                                           @PathVariable Integer pageSize) {
        VoFriendsReq voFriendsReq = new VoFriendsReq();
        voFriendsReq.setPageSize(pageSize);
        voFriendsReq.setPageIndex(pageIndex);
        voFriendsReq.setUserId(userId);
        voFriendsReq.setType(type);
        return brokerBounsBiz.pcFriendsTender(voFriendsReq);
    }


    @ApiOperation("邀请统计")
    @GetMapping("/invite/pc/v2/statistic")
    public ResponseEntity<VoViewInviteAwardStatisticsWarpRes> statistic(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return brokerBounsBiz.statistic(userId);
    }

    @ApiOperation("邀请统计--列表")
    @PostMapping("/invite/pc/v2/brokerBouns/list")
    public ResponseEntity<VoViewBrokerBounsWarpRes> statistic(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                              VoFriendsTenderReq friendsTenderReq) {
        friendsTenderReq.setUserId(userId);
        return brokerBounsBiz.pcBrokerBounsList(friendsTenderReq);
    }


    @ApiOperation("邀请好友首页页面")
    @GetMapping("invite/pc/v2/shareRegister")
    public ResponseEntity shareRegister(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        try {
            Map<String, Object> resultMaps = brokerBounsBiz.shareRegister(userId);
            return ResponseEntity.ok(resultMaps);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败"));
        }
    }

}