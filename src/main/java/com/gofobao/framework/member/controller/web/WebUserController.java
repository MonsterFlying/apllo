package com.gofobao.framework.member.controller.web;

import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.biz.UserEmailBiz;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.entity.UserInfo;
import com.gofobao.framework.member.vo.request.VoBindEmailReq;
import com.gofobao.framework.member.vo.request.VoBindPhone;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhoneReq;
import com.gofobao.framework.member.vo.request.VoUserInfoUpdateReq;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.member.vo.response.VoSignInfoResp;
import com.gofobao.framework.member.vo.response.pc.UserInfoExt;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Max on 17/5/16.
 */
@RestController
@RequestMapping
public class WebUserController {

    @Autowired
    UserPhoneBiz userPhoneBiz;

    @Autowired
    UserEmailBiz userEmailBiz ;

    @Autowired
    UserThirdBiz userThirdBiz ;

    @Autowired
    UserBiz userBiz ;



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
    public ResponseEntity<VoBasicUserInfoResp> bindPhone( @ModelAttribute @Valid  VoBindPhone voBindPhone,
                                                          @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
       return userPhoneBiz.bindPhone(voBindPhone, userId) ;
    }


    @ApiOperation("绑定邮箱")
    @PostMapping("/user/pc/email/bind")
    public ResponseEntity<VoBasicUserInfoResp> bindEmail( @ModelAttribute @Valid VoBindEmailReq voBindEmailReq,
                                                          @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return userEmailBiz.bindEmail(voBindEmailReq, userId) ;
    }

    @ApiOperation("获取签约状态")
    @PostMapping("/user/pc/sign")
    public ResponseEntity<VoSignInfoResp> querySigned(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return userThirdBiz.querySigned(userId) ;
    }



    @ApiOperation("更换手机下一步短信判断")
    @GetMapping("/user/pc/phone/switchVerify/{smsCode}")
    public ResponseEntity<VoBaseResp> verfyUnBindPhoneMessage(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                              @PathVariable("smsCode") String smsCode){
        return userPhoneBiz.verfyUnBindPhoneMessage(userId, smsCode) ;
    }



    @ApiOperation("获取用户配置信息")
    @PostMapping("/user/pc/configInfo")
    public ResponseEntity<VoBasicUserInfoResp> getUserInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return userBiz.userInfo(userId) ;
    }


    @ApiOperation("用戶扩展信息")
    @PostMapping("/user/pc/userInfoExt")
    public ResponseEntity<UserInfoExt> userExt(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return userBiz.pcUserInfo(userId);
    }

    @ApiOperation("用戶扩展修改")
    @PostMapping("/user/pc/userInfoExt/update")
    public ResponseEntity<VoBaseResp> userInfoUpdate(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                      @ModelAttribute VoUserInfoUpdateReq VoUserInfoUpdateReq){
        VoUserInfoUpdateReq.setUserId(userId);
        return userBiz.pcUserInfoUpdate(VoUserInfoUpdateReq);
    }


}