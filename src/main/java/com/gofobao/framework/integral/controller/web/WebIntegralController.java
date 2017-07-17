package com.gofobao.framework.integral.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.integral.biz.IntegralBiz;
import com.gofobao.framework.integral.vo.request.VoIntegralTakeReq;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import com.gofobao.framework.integral.vo.response.VoListIntegralResp;
import com.gofobao.framework.integral.vo.response.pc.VoViewIntegralWarpRes;
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
public class WebIntegralController {

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
    @ApiOperation("获取积分统计")
    @PostMapping("pub/pc/integral/statistics")
    public ResponseEntity<VoListIntegralResp> list(@Valid @ModelAttribute VoListIntegralReq voListIntegralReq,
                                                   @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        //pc 请求
        voListIntegralReq.setType(1);
        voListIntegralReq.setUserId(userId);
        return integralBiz.list(voListIntegralReq);
    }
    /**
     * 获取积分列表
     *
     * @param voListIntegralReq
     * @return
     */
    @ApiOperation("获取积分列表")
    @PostMapping("pub/pc/integral/list")
    public ResponseEntity<VoViewIntegralWarpRes> pcList(@Valid @ModelAttribute VoListIntegralReq voListIntegralReq,
                                                   @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        voListIntegralReq.setUserId(userId);
        return integralBiz.pcIntegralList(voListIntegralReq);
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
    @PostMapping(value = "pub/pc/integral/doTakeRates")
    public ResponseEntity<VoBaseResp> doTakeRates(@Valid @ModelAttribute VoIntegralTakeReq voIntegralTakeReq,
                                                  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voIntegralTakeReq.setUserId(userId);
        try {
            return integralBiz.doTakeRates(voIntegralTakeReq);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ResponseEntity
                .badRequest()
                .body(VoBaseResp.error(VoBaseResp.ERROR, "积分折现失败!"));

    }

    /**
     * 积分折现系数说明
     *
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "积分折现系数说明")
    @PostMapping(value = "pub/pc/integral/takeRatesDesc")
    public ResponseEntity<String> takeRatesDesc() throws Exception {
        Map<String, Object> paranMap = new HashMap<>() ;
        String content = thymeleafHelper.build("integral/takeRateDesc", paranMap) ;
        return ResponseEntity.ok(content);
    }
}
