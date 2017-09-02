package com.gofobao.framework.member.controller.web;

import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.RandomUtil;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.biz.UserEmailBiz;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.entity.Vip;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.*;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.member.vo.response.VoSignInfoResp;
import com.gofobao.framework.member.vo.response.pc.UserInfoExt;
import com.gofobao.framework.member.vo.response.pc.VipInfoRes;
import com.gofobao.framework.member.vo.response.pc.VoViewServiceUserListWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

/**
 * Created by Max on 17/5/16.
 */
@RestController
@RequestMapping
public class WebUserController {

    @Autowired
    UserPhoneBiz userPhoneBiz;

    @Autowired
    UserEmailBiz userEmailBiz;

    @Autowired
    UserThirdBiz userThirdBiz;
    @Autowired
    private UserService userService;
    @Autowired
    UserBiz userBiz;


    @ApiOperation("更改手机号")
    @PostMapping("/user/pc/phone/switch")
    public ResponseEntity<VoBasicUserInfoResp> bindSwitchPhone(@Valid @ModelAttribute VoBindSwitchPhoneReq voBindSwitchPhoneReq,
                                                               @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voBindSwitchPhoneReq.setUserId(userId);
        return userPhoneBiz.bindSwitchPhone(voBindSwitchPhoneReq);
    }

    @ApiOperation("判断 邮箱/手机/用户名 是否可用")
    @PostMapping("/pub/user/pc/info/checkOnly")
    public ResponseEntity<VoBaseResp> checkOnlyForUserInfo(@Valid @ModelAttribute VoJudgmentAvailableReq VoJudgmentAvailableReq) {
        return userPhoneBiz.checkOnlyForUserInfo(VoJudgmentAvailableReq);
    }


    @ApiOperation("绑定手机")
    @PostMapping("/user/pc/phone/bind")
    public ResponseEntity<VoBasicUserInfoResp> bindPhone(@ModelAttribute @Valid VoBindPhone voBindPhone,
                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userPhoneBiz.bindPhone(voBindPhone, userId);
    }


    @ApiOperation("绑定邮箱")
    @PostMapping("/user/pc/email/bind")
    public ResponseEntity<VoBasicUserInfoResp> bindEmail(@ModelAttribute @Valid VoBindEmailReq voBindEmailReq,
                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userEmailBiz.bindEmail(voBindEmailReq, userId);
    }

    @ApiOperation("获取签约状态")
    @PostMapping("/user/pc/sign")
    public ResponseEntity<VoSignInfoResp> querySigned(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.querySigned(userId);
    }


    @ApiOperation("更换手机下一步短信判断")
    @GetMapping("/user/pc/phone/switchVerify/{smsCode}")
    public ResponseEntity<VoBaseResp> verfyUnBindPhoneMessage(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                              @PathVariable("smsCode") String smsCode) {
        return userPhoneBiz.verfyUnBindPhoneMessage(userId, smsCode);
    }


    @ApiOperation("获取用户配置信息")
    @PostMapping("/user/pc/configInfo")
    public ResponseEntity<VoBasicUserInfoResp> getUserInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userBiz.userInfo(userId);
    }


    @ApiOperation("用戶扩展信息")
    @PostMapping("/user/pc/userInfoExt")
    public ResponseEntity<UserInfoExt> userExt(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userBiz.pcUserInfo(userId);
    }

    @ApiOperation("用戶扩展修改")
    @PostMapping("/user/pc/userInfoExt/update")
    public ResponseEntity<VoBaseResp> userInfoUpdate(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                     @ModelAttribute VoUserInfoUpdateReq VoUserInfoUpdateReq) {
        VoUserInfoUpdateReq.setUserId(userId);
        return userBiz.pcUserInfoUpdate(VoUserInfoUpdateReq);
    }

    @ApiOperation("设置交易密码")
    @PostMapping("/user/pc/payPayPassWord/save")
    public ResponseEntity<VoBaseResp> userInfoUpdate(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                     @ModelAttribute VoSettingTranPassWord tranPassWord) {
        tranPassWord.setUserId(userId);
        return userBiz.saveUserTranPassWord(tranPassWord);
    }

    @ApiOperation("重置交易密码")
    @PostMapping("/user/pc/rest/payPassWord")
    public ResponseEntity<VoBaseResp> restPayPassWord(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                     @ModelAttribute VoRestPayPassWord tranPassWord) {
        tranPassWord.setUserId(userId);
        return userBiz.restPayPassWord(tranPassWord);
    }



    @ApiOperation("申请vip")
    @PostMapping("/user/pc/v2/applyFor/vip")
    public ResponseEntity<VoBaseResp> applyFor(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                               VoApplyForVipReq forVipReq) {
        Vip vip = new Vip();
        vip.setUserId(userId);
        vip.setVerifyAt(new Date());
        vip.setKefuId(forVipReq.getServiceUserId());
        vip.setExpireAt(DateHelper.addYears(new Date(), 1));
        vip.setCreatedAt(new Date());
        vip.setRemark("");
        vip.setStatus(1);
        return userBiz.saveVip(vip);
    }

    @ApiOperation("客服列表")
    @GetMapping("/user/pc/v2/service/list")
    public ResponseEntity<VoViewServiceUserListWarpRes> serviceUserList() {
        return userBiz.serviceUserList();
    }


    @ApiOperation("vip信息")
    @GetMapping("/user/pc/v2/vip/info")
    public ResponseEntity<VipInfoRes> vipInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userBiz.vipInfo(userId);
    }

    @ApiOperation("上传头像")
    @PostMapping(value = "/user/uploading/avatar")
    public void uploadAvatar(@RequestParam("file") MultipartFile upfile,
                             HttpServletResponse response,
                             @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        Users users=userService.findById(userId);
        if(ObjectUtils.isEmpty(users)){
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
        }else if (!StringUtils.isEmpty(upfile)) {
            //循环获取file数组中得文件
            String imageName = "avatar/"+RandomUtil.getRandomString(20);
            byte[] fileByte = upfile.getBytes();
            Map<String, Object> result = userBiz.uploadAvatar(fileByte, imageName,users);
            if ((Boolean) result.get("result")) {
                response.setStatus(HttpStatus.SC_OK);
            } else {
                response.setStatus(HttpStatus.SC_BAD_REQUEST);
            }
            PrintWriter printWriter = response.getWriter();
            printWriter.print(new Gson().toJson(result));

        }
    }
}
