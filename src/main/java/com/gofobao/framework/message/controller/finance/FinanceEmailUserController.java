package com.gofobao.framework.message.controller.finance;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.request.VoAnonEmailReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 邮箱验证码
 * Created by Max on 17/5/17.
 */
@RestController
@RequestMapping("/email/finance")
public class FinanceEmailUserController {

    @Autowired
    private MessageBiz messageBiz ;

    @ApiOperation("发送用户绑定邮箱")
    @PostMapping("/bindEmail")
    public ResponseEntity<VoBaseResp> sendBindEmail(HttpServletRequest request,
                                                    @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                    @Valid @ModelAttribute VoAnonEmailReq voAnonEmailReq){
        return messageBiz.sendBindEmail(request, voAnonEmailReq, userId) ;
    }
}

