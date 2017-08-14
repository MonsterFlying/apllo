package com.gofobao.framework.finance.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.vo.request.VoFinancePlanTender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/8/10.
 */
@RestController
@Api(description = "理财计划功能模块")
public class WebFinanceController {

    @Autowired
    private FinancePlanBiz financePlanBiz;

    /**
     * 理财计划投标
     *
     * @param voFinancePlanTender
     * @return
     */
    @ApiOperation("获取用户广富币列表")
    public ResponseEntity<VoBaseResp> financePlanTender(@Valid VoFinancePlanTender voFinancePlanTender){
        return financePlanBiz.financePlanTender(voFinancePlanTender);
    }
}
