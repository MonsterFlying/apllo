package com.gofobao.framework.member.controller.finance;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserPasswordBiz;
import com.gofobao.framework.member.vo.request.VoFindPasswordReq;
import com.gofobao.framework.member.vo.request.VoModifyPasswordReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by admin on 2017/5/17.
 */

@RestController
public class FinancePasswordController {

    @Autowired
    private UserPasswordBiz userPasswordBiz;

    /**
     * 用户修改密码
     *
     * @param voModifyPasswordReq
     * @return
     */
    @ApiOperation("用户修改密码")
    @PostMapping("/user/finance/password/modify")
    public ResponseEntity<VoBaseResp> modifyPassword(@Valid @ModelAttribute VoModifyPasswordReq voModifyPasswordReq,
                                                     @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        return userPasswordBiz.modifyPassword(userId, voModifyPasswordReq);
    }

    /**
     * 忘记密码
     *
     * @param voFindPasswordReq
     * @return
     */
    @ApiOperation("用户忘记密码")
    @PostMapping("/pub/user/finance/password/find")
    public ResponseEntity<VoBaseResp> findPassword(@Valid @ModelAttribute VoFindPasswordReq voFindPasswordReq) {
        return userPasswordBiz.findPassword(voFindPasswordReq);
    }


}
