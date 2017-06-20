package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import com.gofobao.framework.asset.vo.request.VoBankApsReq;
import com.gofobao.framework.asset.vo.request.VoCashReq;
import com.gofobao.framework.asset.vo.response.VoBankApsWrapResp;
import com.gofobao.framework.asset.vo.response.VoCashLogDetailResp;
import com.gofobao.framework.asset.vo.response.VoCashLogWrapResp;
import com.gofobao.framework.asset.vo.response.VoPreCashResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 *
 * Created by Max on 17/6/8.
 */
@Controller
@Api(value = "提现")
public class CashWebController {

    @Autowired
    private CashDetailLogBiz cashDetailLogBiz;



    @GetMapping("/pub/cash/show/{seqNo}")
    public String showCash(@PathVariable("seqNo") String  seqNo, Model model){
        return cashDetailLogBiz.showCash(seqNo, model) ;
    }
}
