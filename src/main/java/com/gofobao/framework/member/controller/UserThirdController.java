package com.gofobao.framework.member.controller;

import com.gofobao.framework.borrow.vo.request.VoAdminModifyPasswordResp;
import com.gofobao.framework.borrow.vo.request.VoAdminOpenAccountResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.vo.request.VoOpenAccountReq;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.member.vo.response.VoOpenAccountResp;
import com.gofobao.framework.member.vo.response.VoPreOpenAccountResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.vo.request.VoCommonReq;
import com.gofobao.framework.tender.vo.request.VoPublishRedReq;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 存管账户
 * Created by Max on 17/5/22.
 */
@RestController
public class UserThirdController {
    @Autowired
    private UserThirdBiz userThirdBiz;


    @ApiOperation("银行存管前置请求第一步")
    @PostMapping("/user/third/preOpenAccout")
    public ResponseEntity<VoPreOpenAccountResp> preOpenAccout(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.preOpenAccount(userId);
    }


    @ApiOperation("银行存管开户")
    @PostMapping("/user/third/openAccout")
    public ResponseEntity<VoOpenAccountResp> openAccount(HttpServletRequest httpServletRequest, @Valid @ModelAttribute VoOpenAccountReq voOpenAccountReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.openAccount(voOpenAccountReq, userId, httpServletRequest);
    }


    @ApiOperation("银行存管密码管理")
    @PostMapping("/user/third/modifyOpenAccPwd")
    public ResponseEntity<VoHtmlResp> modifyOpenAccPwd(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.modifyOpenAccPwd(httpServletRequest, userId);
    }


    @ApiOperation("绑定银行卡")
    @GetMapping("/user/third/bind/bank/{bankNo}")
    public ResponseEntity<VoHtmlResp> bindBank(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable("bankNo") String bankNo) {
        return userThirdBiz.bindBank(httpServletRequest, userId, bankNo);
    }


    @ApiOperation("解除银行卡绑定")
    @PostMapping("/user/third/del/bank")
    public ResponseEntity<VoBaseResp> delBank(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.delBank(httpServletRequest, userId);
    }


    @ApiOperation("银行卡绑定回调")
    @PostMapping("/pub/third/bank/bind/callback")
    public ResponseEntity<String> bankBindCallback(HttpServletRequest httpServletRequest) {
        return userThirdBiz.bankBindCallback(httpServletRequest);
    }

    @ApiOperation("银行存管页面中的忘记密码")
    @GetMapping("/pub/third/password/{encode}/{channel}")
    public ResponseEntity<String> publicPasswordModify(HttpServletRequest httpServletRequest, @PathVariable("encode") String encode, @PathVariable("channel") String channel) {
        return userThirdBiz.publicPasswordModify(httpServletRequest, encode, channel);
    }

    @ApiOperation("银行存管页面中忘记密码回调")
    @PostMapping("/pub/user/third/modifyOpenAccPwd/callback/{type}")
    public ResponseEntity<String> modifyOpenAccPwdCallback(HttpServletRequest request, HttpServletResponse response, @PathVariable Integer type) {
        return userThirdBiz.modifyOpenAccPwdCallback(request, response, type);
    }

    @ApiOperation("开通自动投标协议")
    @PostMapping("/user/third/autoTender")
    public ResponseEntity<VoHtmlResp> autoTender(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.autoTender(httpServletRequest, userId);
    }

    @ApiOperation("开通自动转让协议")
    @PostMapping("/user/third/autoTranfter")
    public ResponseEntity<VoHtmlResp> autoTranfter(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.autoTranfter(httpServletRequest, userId);
    }


    @PostMapping("/pub/user/third/autoTender/callback")
    public ResponseEntity<String> autoTenderCallback(HttpServletRequest request, HttpServletResponse response) {
        return userThirdBiz.autoTenderCallback(request, response);
    }


    @PostMapping("/pub/user/third/autoTranfer/callback")
    public ResponseEntity<String> autoTranferCallback(HttpServletRequest request, HttpServletResponse response) {
        return userThirdBiz.autoTranferCallback(request, response);
    }


    @ApiOperation("后台开户")
    @PostMapping("/pub/admin/third/openAccout")
    public ResponseEntity<VoHtmlResp> adminOpenAccount(HttpServletRequest httpServletRequest, @Valid @ModelAttribute VoAdminOpenAccountResp voAdminOpenAccountResp) {
        return userThirdBiz.adminOpenAccount(voAdminOpenAccountResp, httpServletRequest);
    }

    @PostMapping("/pub/admin/third/openAccout/callback/{userId}")
    public ResponseEntity<String> adminOpenAccountCallback(HttpServletRequest httpServletRequest, @PathVariable("userId") Long userId) {
        return userThirdBiz.adminOpenAccountCallback(httpServletRequest, userId);
    }


    @ApiOperation("后台调用初始化密码")
    @GetMapping("/pub/initPassword/{encode}/{channel}")
    public ResponseEntity<String> adminPasswordInit(HttpServletRequest httpServletRequest, @PathVariable("encode") String encode, @PathVariable("channel") String channel) {
        return userThirdBiz.adminPasswordInit(httpServletRequest, encode, channel);
    }

    @ApiOperation("后台修改借款人银行密码")
    @PostMapping("/pub/admin/third/modifyOpenAccPwd")
    public ResponseEntity<VoHtmlResp> adminModifyOpenAccPwd(HttpServletRequest httpServletRequest, @Valid @ModelAttribute VoAdminModifyPasswordResp voAdminModifyPasswordResp) {
        return userThirdBiz.adminModifyOpenAccPwd(httpServletRequest, voAdminModifyPasswordResp);
    }


    @ApiOperation("获取企业账户信息")
    @PostMapping("pub/admin/companyAccountInfo")
    public ResponseEntity<VoBaseResp> companyAccountInfo(@ModelAttribute VoCommonReq voCommonReq) throws Exception {
        String paramStr = voCommonReq.getParamStr();
        if (!SecurityHelper.checkSign(voCommonReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "验证参数, 不通过!"));
        }
        return userThirdBiz.companyAccountInfo(voCommonReq.getParamStr());
    }
}
