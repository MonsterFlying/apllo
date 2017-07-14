package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.vo.response.VoBankListResp;
import com.gofobao.framework.asset.vo.response.VoBankTypeInfoResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by Zeke on 2017/5/22.
 */
@RestController
public class WebBankAccountController {
    @Autowired
    private BankAccountBiz bankAccountBiz ;

    @GetMapping("/bank/pc/V2/list")
    @ApiOperation("银行卡列表")
    public ResponseEntity<VoBankListResp> list(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return bankAccountBiz.list(userId) ;
    }

}
