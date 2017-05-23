package com.gofobao.framework.integral.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.integral.biz.IntegralBiz;
import com.gofobao.framework.integral.vo.request.VoIntegralTakeReq;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/22.
 */
@RestController
@RequestMapping
@Api(description = "积分模块")
public class IntegralController {

    @Autowired
    private IntegralBiz integralBiz;
    @Autowired
    private ThymeleafHelper thymeleafHelper;

    /**
     * 获取积分列表
     *
     * @param voListIntegralReq
     * @return
     */
    @ApiOperation("获取积分列表")
    @PostMapping("/integral/list")
    public ResponseEntity<VoBaseResp> list(@Valid @ModelAttribute VoListIntegralReq voListIntegralReq,@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        voListIntegralReq.setUserId(userId);
        return integralBiz.list(voListIntegralReq);
    }

    /**
     * 积分兑换
     *
     * @param userId
     * @param voIntegralTakeReq
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "积分兑换")
    @PostMapping(value = "/integral/doTakeRates")
    public ResponseEntity<VoBaseResp> doTakeRates(@Valid @ModelAttribute VoIntegralTakeReq voIntegralTakeReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voIntegralTakeReq.setUserId(userId);
        return integralBiz.doTakeRates(voIntegralTakeReq);
    }

    /**
     * 积分折现系数说明
     *
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "积分折现系数说明")
    @PostMapping(value = "/takeRatesDesc")
    public ResponseEntity<String> takeRatesDesc() throws Exception {
        Map<String, Object> paranMap = new HashMap<>() ;
        String content = thymeleafHelper.build("integral/takeRateDesc", paranMap) ;
        return ResponseEntity.ok(content);
    }
}
