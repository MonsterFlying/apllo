package com.gofobao.framework.product.controller;

import com.gofobao.framework.product.biz.ProductBiz;
import com.gofobao.framework.product.vo.request.VoFindProductPlanList;
import com.gofobao.framework.product.vo.response.VoViewFindProductPlanListRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/11/10.
 */
@RestController
@RequestMapping
@Slf4j
@Api(description = "商品控制器")
public class ProductController {

    @Autowired
    private ProductBiz productBiz;

    /**
     * 查询首页广富送列表
     *
     * @param voFindProductPlanList
     * @return
     */
    @PostMapping("/pub/v2/product/plan/list")
    @ApiOperation("查询首页广富送列表")
    public ResponseEntity<VoViewFindProductPlanListRes> findProductPlanList(@Valid @ModelAttribute VoFindProductPlanList voFindProductPlanList) {
        return productBiz.findProductPlanList(voFindProductPlanList);
    }
}
