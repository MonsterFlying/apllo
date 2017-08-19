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
import com.gofobao.framework.helper.DateHelper;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.qiniu.storage.Recorder;
import io.swagger.annotations.ApiOperation;
import org.apache.http.HttpStatus;
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
import java.util.Date;
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
    public void uploadAvatar(@RequestParam("file") MultipartFile file,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
/*
        Map<String, Object> errorMap = Maps.newHashMap();


        Auth auth = Auth.create(accessKey, secretKey);

        //第二种方式: 自动识别要上传的空间(bucket)的存储区域是华东、华北、华南。
        Zone z = Zone.autoZone();
        Configuration c = new Configuration(z);

        //创建上传对象
        UploadManager uploadManager = new UploadManager(c);

        String authStr=auth.uploadToken(bucketname, key, 3600);

        // 覆盖上传
        public String getUpToken() {
            //<bucket>:<key>，表示只允许用户上传指定key的文件。在这种格式下文件默认允许“修改”，已存在同名资源则会被本次覆盖。
            //如果希望只能上传指定key的文件，并且不允许修改，那么可以将下面的 insertOnly 属性值设为 1。
            //第三个参数是token的过期时间
            return auth.uploadToken(bucketname, key, 3600, new StringMap().put("insertOnly", 1));
        }

        public void upload() throws IOException {
            try {
                //调用put方法上传，这里指定的key和上传策略中的key要一致
                Response res = uploadManager.put(filePath, key, getUpToken());
                //打印返回的信息
                System.out.println(res.bodyString());
            } catch (QiniuException e) {
                Response r = e.response;
                // 请求失败时打印的异常的信息
                System.out.println(r.toString());
                try {
                    //响应的文本信息
                    System.out.println(r.bodyString());
                } catch (QiniuException e1) {
                    //ignore
                }
            }
        }
    }
    // 覆盖上传
    public String getUpToken(Auth auth) {
        //<bucket>:<key>，表示只允许用户上传指定key的文件。在这种格式下文件默认允许“修改”，已存在同名资源则会被本次覆盖。
        //如果希望只能上传指定key的文件，并且不允许修改，那么可以将下面的 insertOnly 属性值设为 1。
        //第三个参数是token的过期时间
        return ;*/
    }

}
