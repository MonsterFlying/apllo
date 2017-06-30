package com.gofobao.framework.member.controller;

import com.gofobao.framework.award.vo.response.VoViewShareRegiestRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.QRCodeHelper;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.member.biz.BrokerBounsBiz;
import com.gofobao.framework.member.vo.request.VoFriendsReq;
import com.gofobao.framework.member.vo.response.VoViewFriendsTenderInfoWarpRes;
import com.gofobao.framework.member.vo.response.VoViewInviteAwardStatisticsWarpRes;
import com.gofobao.framework.member.vo.response.VoViewInviteFriendersWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by admin on 2017/6/7.
 */

@Slf4j
@RequestMapping("")
@Api(description = "我的邀请")
@RestController
public class FriendsController {

    @Autowired
    private BrokerBounsBiz brokerBounsBiz;

    @Autowired
    private ThymeleafHelper thymeleafHelper;


    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${gofobao.webDomain}")
    private String webDomain;

    @ApiOperation("邀请好友列表")
    @GetMapping("/invite/v2/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewInviteFriendersWarpRes> list(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                             @PathVariable Integer pageIndex,
                                                             @PathVariable Integer pageSize) {
        VoFriendsReq voFriendsReq = new VoFriendsReq();
        voFriendsReq.setPageSize(pageSize);
        voFriendsReq.setPageIndex(pageIndex);
        voFriendsReq.setUserId(userId);
        return brokerBounsBiz.list(voFriendsReq);
    }

    @ApiOperation("邀请统计")
    @GetMapping("/invite/v2/statistic")
    public ResponseEntity<VoViewInviteAwardStatisticsWarpRes> statistic(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return brokerBounsBiz.statistic(userId);
    }

    @ApiOperation("邀请好友投标列表")
    @GetMapping("/invite/v2/first/tender/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewFriendsTenderInfoWarpRes> firstTenderList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                          @PathVariable Integer pageIndex,
                                                                          @PathVariable Integer pageSize) {
        VoFriendsReq voFriendsReq = new VoFriendsReq();
        voFriendsReq.setPageSize(pageSize);
        voFriendsReq.setPageIndex(pageIndex);
        voFriendsReq.setUserId(userId);
        return brokerBounsBiz.firstTender(voFriendsReq);
    }

    @ApiOperation("邀请好友首页页面")
    @GetMapping("invite/v2/shareRegister")
    public ResponseEntity<VoViewShareRegiestRes> shareRegister(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        String content;
        VoViewShareRegiestRes res = VoBaseResp.ok("查询成功", VoViewShareRegiestRes.class);
        try {
            Map<String, Object> resultMaps = brokerBounsBiz.shareRegister(userId);
            res.setCodeUrl(resultMaps.get("QRCodeURL").toString());
            res.setTitle("邀请好友投资,奖励送不停");
            res.setDesc("新手福利,注册即送1000元投标体验金+加息0.5%-3%");
            res.setRequestHtmlUrl(h5Domain + "invite/v2/shareRegister");
            res.setIcon(webDomain + "/images/bankLogo/logo.png");
            content = thymeleafHelper.build("user/friends", resultMaps);
            res.setHtml(content);
        } catch (Exception e) {
            e.printStackTrace();
            content = thymeleafHelper.build("load_error", null);
        }
        res.setHtml(content);

        return ResponseEntity.ok(res);
    }


    /**
     * 获取二维码图片
     *
     * @param response
     */
    @RequestMapping(value = "invite/qrcode/getInviteFriendQRCode", method = RequestMethod.GET)
    @ApiOperation(value = "获取二维码接口", notes = "获取二维码接口")
    public void getInviteFriendQRCode(@RequestParam("inviteCode") String inviteCode, HttpServletResponse response) {

        OutputStream out = null;
        try {
            out = response.getOutputStream();
        } catch (Exception e) {
            log.error(String.format("获取二维码接口：%s", e.getMessage()));
        }
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");  // Set standard HTTP/1.1 no-cache headers.
        response.addHeader("Cache-Control", "post-check=0, pre-check=0"); // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.setHeader("Pragma", "no-cache");  // Set standard HTTP/1.0 no-cache header.
        response.setContentType("image/jpeg"); // return a jpeg

        try {
            InputStream in = FriendsController.class.getResourceAsStream("/static/img/logo.png");
            http:
//192.168.1.235:8000/#/auth/register?_k=bpjq7r.createQRCodeTStream(h5Domain+"/#/auth/register?shareRegisterCode="+inviteCode, in, 50, 50, out);
            QRCodeHelper.createQRCodeTStream(h5Domain + "/#/auth/register?shareRegisterCode=" + inviteCode, in, 50, 50, out);
            out.flush();
        } catch (Exception e) {
            log.error(String.format("获取二维码接口：%s", e.getMessage()));
        }


    }


}
