package com.gofobao.framework.product.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.biz.ProductCollectBiz;
import com.gofobao.framework.product.service.ProductCollectService;
import com.gofobao.framework.product.vo.request.VoCancelCollectProduct;
import com.gofobao.framework.product.vo.request.VoCollectProduct;
import com.gofobao.framework.product.vo.request.VoFindProductCollectList;
import com.gofobao.framework.product.vo.response.VoViewFindProductCollectListRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/11/22.
 */
@RestController
@RequestMapping("/finance")
@Slf4j
@Api(description = "商品收藏控制器")
public class FinanceProductCollectColltroller {

    @Autowired
    private ProductCollectBiz productCollectBiz;

    /**
     * 收藏列表
     */
    @PostMapping("/v2/product/collect/list")
    @ApiOperation("收藏列表")
    public ResponseEntity<VoViewFindProductCollectListRes> findProductCollectList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoFindProductCollectList voFindProductCollectList) {
        voFindProductCollectList.setUserId(userId);
        return productCollectBiz.findProductCollectList(voFindProductCollectList);
    }

    /**
     * 收藏商品
     */
    @PostMapping("/v2/product/collect/collect")
    @ApiOperation("收藏商品")
    public ResponseEntity<VoBaseResp> collectProduct(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoCollectProduct voCollectProduct) {
        voCollectProduct.setUserId(userId);
        return productCollectBiz.collectProduct(voCollectProduct);
    }

    /**
     * 取消收藏商品
     */
    @PostMapping("/v2/product/collect/cancel")
    @ApiOperation("收藏商品")
    public ResponseEntity<VoBaseResp> cancelCollectProduct(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoCancelCollectProduct voCancelCollectProduct) {
        voCancelCollectProduct.setUserId(userId);
        return productCollectBiz.cancelCollectProduct(voCancelCollectProduct);
    }
}
