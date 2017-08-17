package com.gofobao.framework.currency.controller.web;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Zeke on 2017/5/23.
 */
@RestController
@RequestMapping
@Api(description = "pc:广富币模块")
public class WebCurrencyController {

 /*   @Autowired
    private CurrencyBiz currencyBiz;

    @ApiOperation("pc:获取用户广富币列表")
    @PostMapping("pc/currency/list")
    public ResponseEntity<VoListCurrencyResp> pcList(@Valid @ModelAttribute VoListCurrencyReq voListCurrencyReq,
                                             @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        voListCurrencyReq.setUserId(userId);
        return currencyBiz.list(voListCurrencyReq);
    }

    *//**
     * 兑换广福币
     *
     * @return
     *//*
    @ApiOperation("pc:兑换广富币")
    @PostMapping("pc/currency/convert")
    public ResponseEntity<VoBaseResp> pcConvert(@Valid @ModelAttribute VoConvertCurrencyReq voConvertCurrencyReq,
                                                @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        voConvertCurrencyReq.setUserId(userId);
        try {
            return currencyBiz.convert(voConvertCurrencyReq);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR,"兑换广富币失败!"));
    }*/
}
