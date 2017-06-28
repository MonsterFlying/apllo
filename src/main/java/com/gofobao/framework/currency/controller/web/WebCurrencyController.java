package com.gofobao.framework.currency.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.currency.biz.CurrencyBiz;
import com.gofobao.framework.currency.vo.request.VoConvertCurrencyReq;
import com.gofobao.framework.currency.vo.request.VoListCurrencyReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/5/23.
 */
@RestController
@RequestMapping
@Api(description = "pc:广富币模块")
public class WebCurrencyController {

    @Autowired
    private CurrencyBiz currencyBiz;

    @ApiOperation("pc:获取用户广富币列表")
    @PostMapping("pc/currency/list")
    public ResponseEntity<VoBaseResp> pcList(@Valid @ModelAttribute VoListCurrencyReq voListCurrencyReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        voListCurrencyReq.setUserId(userId);
        return currencyBiz.list(voListCurrencyReq);
    }

    /**
     * 兑换广福币
     *
     * @return
     */
    @ApiOperation("pc:兑换广富币")
    @PostMapping("pc/currency/convert")
    public ResponseEntity<VoBaseResp> pcConvert(@Valid @ModelAttribute VoConvertCurrencyReq voConvertCurrencyReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        voConvertCurrencyReq.setUserId(userId);
        try {
            return currencyBiz.convert(voConvertCurrencyReq);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"兑换广富币失败!"));
    }
}
