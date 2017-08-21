package com.gofobao.framework.member.controller;

import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.biz.UserEmailBiz;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.vo.request.VoBindEmailReq;
import com.gofobao.framework.member.vo.request.VoBindPhone;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhoneReq;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.member.vo.response.VoOpenAccountInfo;
import com.gofobao.framework.member.vo.response.VoSignInfoResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by Max on 17/5/16.
 */
@RestController
@RequestMapping
public class UserController {

    @Autowired
    UserPhoneBiz userPhoneBiz;

    @Autowired
    UserEmailBiz userEmailBiz;

    @Autowired
    UserThirdBiz userThirdBiz;

    @Autowired
    UserBiz userBiz;


    @Value("${qiniu.sk}")
    private String secretKey;

    @Value("${qiniu.ak}")
    private String accessKey;

    @Value("${qiniu.domain}")
    private String qiNiuDomain;

    @Value("${qiniu.bucket}")
    private String bucket;


    @ApiOperation("更改手机号")
    @PostMapping("/user/phone/switch")
    public ResponseEntity<VoBasicUserInfoResp> bindSwitchPhone(@Valid @ModelAttribute VoBindSwitchPhoneReq voBindSwitchPhoneReq,
                                                               @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voBindSwitchPhoneReq.setUserId(userId);
        return userPhoneBiz.bindSwitchPhone(voBindSwitchPhoneReq);
    }

    @ApiOperation("判断 邮箱/手机/用户名 是否可用")
    @PostMapping("/pub/user/info/checkOnly")
    public ResponseEntity<VoBaseResp> checkOnlyForUserInfo(@Valid @ModelAttribute VoJudgmentAvailableReq VoJudgmentAvailableReq) {
        return userPhoneBiz.checkOnlyForUserInfo(VoJudgmentAvailableReq);
    }


    @ApiOperation("绑定手机")
    @PostMapping("/user/phone/bind")
    public ResponseEntity<VoBasicUserInfoResp> bindPhone(@ModelAttribute @Valid VoBindPhone voBindPhone,
                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userPhoneBiz.bindPhone(voBindPhone, userId);
    }


    @ApiOperation("绑定邮箱")
    @PostMapping("/user/email/bind")
    public ResponseEntity<VoBasicUserInfoResp> bindEmail(@ModelAttribute @Valid VoBindEmailReq voBindEmailReq,
                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userEmailBiz.bindEmail(voBindEmailReq, userId);
    }

    @ApiOperation("获取签约状态")
    @PostMapping("/user/sign")
    public ResponseEntity<VoSignInfoResp> querySigned(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.querySigned(userId);
    }


    @ApiOperation("更换手机下一步短信判断")
    @GetMapping("/user/phone/switchVerify/{smsCode}")
    public ResponseEntity<VoBaseResp> verfyUnBindPhoneMessage(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable("smsCode") String smsCode) {
        return userPhoneBiz.verfyUnBindPhoneMessage(userId, smsCode);
    }

    @ApiOperation("获取用户配置信息")
    @PostMapping("/user/configInfo")
    public ResponseEntity<VoBasicUserInfoResp> getUserInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userBiz.userInfo(userId);
    }


    @ApiOperation("获取存管信息")
    @PostMapping("/user/openAccountInfo")
    public ResponseEntity<VoOpenAccountInfo> getOpenAccountInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userBiz.openAccountInfo(userId);
    }


    @ApiOperation("上传头像")
    @PostMapping(value = "/user/uploading/avatar")
    public void uploadAvatar(@RequestParam("file") MultipartFile upfile,
                             HttpServletResponse response,
                             @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        if (StringUtils.isEmpty(upfile)) {
            //循环获取file数组中得文件

            String fileName = upfile.getOriginalFilename();
            byte[] fileByte = upfile.getBytes();
            Map<String, Object> result = userBiz.uploadAvatar(fileByte, fileName);
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

