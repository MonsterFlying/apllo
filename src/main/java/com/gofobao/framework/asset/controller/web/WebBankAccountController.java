package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.AreaBiz;
import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.biz.UnionLineNumberBiz;
import com.gofobao.framework.asset.vo.request.VoUnionLineNoReq;
import com.gofobao.framework.asset.vo.response.VoBankListResp;
import com.gofobao.framework.asset.vo.response.pc.UnionLineNoWarpRes;
import com.gofobao.framework.asset.vo.response.pc.VoAreaWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Zeke on 2017/5/22.
 */
@RestController
public class WebBankAccountController {
    @Autowired
    private BankAccountBiz bankAccountBiz;
    @Autowired
    private UserThirdBiz userThirdBiz;
    @Autowired
    private AreaBiz areaBiz;
    @Autowired
    private UnionLineNumberBiz lineNumberBiz;

    @GetMapping("/bank/pc/V2/list")
    @ApiOperation("银行卡列表")
    public ResponseEntity<VoBankListResp> list(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return bankAccountBiz.list(userId);
    }


    @ApiOperation("解除银行卡绑定")
    @PostMapping("/user/third/pc/v2/del/bank")
    public ResponseEntity<VoBaseResp> delBank(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.delBank(httpServletRequest, userId);
    }

    @ApiOperation("省市")
    @GetMapping("pub/area/list")
    public ResponseEntity<VoAreaWarpRes> areaList(@Param("id") Integer id) {
        return areaBiz.list(id);
    }

    @ApiOperation("联行号")
    @GetMapping("pub/unionLineNo/list")
    public ResponseEntity<UnionLineNoWarpRes> unionLineNOList(VoUnionLineNoReq unionLineNoReq) {
        return lineNumberBiz.list(unionLineNoReq);
    }

}
