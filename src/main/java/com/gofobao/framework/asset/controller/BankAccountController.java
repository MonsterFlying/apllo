package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AreaBiz;
import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.biz.UnionLineNumberBiz;
import com.gofobao.framework.asset.vo.request.VoUnionLineNoReq;
import com.gofobao.framework.asset.vo.response.VoBankListResp;
import com.gofobao.framework.asset.vo.response.VoBankTypeInfoResp;
import com.gofobao.framework.asset.vo.response.pc.UnionLineNoWarpRes;
import com.gofobao.framework.asset.vo.response.pc.VoAreaWarpRes;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
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
public class BankAccountController {

    @Autowired
    private BankAccountBiz bankAccountBiz ;

    @Autowired
    private AreaBiz areaBiz;

    @Autowired
    private UnionLineNumberBiz lineNumberBiz;


    @GetMapping("/bank/typeinfo/{account}")
    @ApiOperation("根据银行卡获取银行卡基础信息和限额")
    public ResponseEntity<VoBankTypeInfoResp> findTypeInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                           @PathVariable("account") String account){
        return bankAccountBiz.findTypeInfo(userId, account) ;
    }


    @GetMapping("/bank/credit")
    @ApiOperation("额度列表")
    public ResponseEntity<VoHtmlResp> credit(){
        return bankAccountBiz.credit() ;
    }



    @GetMapping("/bank/list")
    @ApiOperation("银行卡列表")
    public ResponseEntity<VoBankListResp> list(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return bankAccountBiz.list(userId) ;
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
