package com.gofobao.framework.member.controller.web;

import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.biz.WebUserThirdBiz;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by master on 2017/8/28.
 */
@Controller
@Api(description = "pc 开通存管")
public class WebUserThirdController {

    @Autowired
    private WebUserThirdBiz userThirdBiz ;

    @ApiOperation("银行存管密码管理")
    @PostMapping("/user/pc/v2/third/modifyOpenAccPwd")
    public ResponseEntity<VoHtmlResp> pcModifyOpenAccPwd(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.modifyOpenAccPwd(httpServletRequest, userId) ;
    }

    @ApiOperation("开通自动投标协议")
    @PostMapping("/user/pc/v2/third/autoTender")
    public ResponseEntity<VoHtmlResp> pcAutoTender(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.autoTender(httpServletRequest, userId) ;
    }

    @ApiOperation("开通自动转让协议")
    @PostMapping("/user/pc/v2/third/autoTranfter")
    public ResponseEntity<VoHtmlResp> pcAutoTranfter(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.autoTranfter(httpServletRequest, userId) ;
    }

}
