package com.gofobao.framework.member.controller;

import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.common.qiniu.common.QiniuException;
import com.gofobao.framework.common.qiniu.common.Zone;
import com.gofobao.framework.common.qiniu.http.Response;
import com.gofobao.framework.common.qiniu.storage.Configuration;
import com.gofobao.framework.common.qiniu.storage.UploadManager;
import com.gofobao.framework.common.qiniu.storage.model.DefaultPutRet;
import com.gofobao.framework.common.qiniu.storage.persistent.FileRecorder;
import com.gofobao.framework.common.qiniu.util.Auth;
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
import com.qiniu.storage.Recorder;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

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
    public void uploadAvatar(@RequestParam("file") MultipartFile file,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {


        Configuration cfg = new Configuration(Zone.autoZone());

        UploadManager uploadManager = new UploadManager(cfg);

        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        Response qresponse;
        try {
            qresponse = uploadManager.put(file.getInputStream(), key, upToken, null, null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(qresponse.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
            String url = qiNiuDomain + putRet.key;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


}
