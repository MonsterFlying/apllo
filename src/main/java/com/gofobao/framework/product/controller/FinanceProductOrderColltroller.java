package com.gofobao.framework.product.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.biz.ProductCollectBiz;
import com.gofobao.framework.product.biz.ProductOrderBiz;
import com.gofobao.framework.product.vo.request.VoCancelCollectProduct;
import com.gofobao.framework.product.vo.request.VoCollectProduct;
import com.gofobao.framework.product.vo.request.VoFindOrderLogisticsDetail;
import com.gofobao.framework.product.vo.request.VoFindProductCollectList;
import com.gofobao.framework.product.vo.response.VoViewFindOrderLogisticsDetailRes;
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
public class FinanceProductOrderColltroller {

    @Autowired
    private ProductOrderBiz productOrderBiz;


    /**
     * 查看物流
     */
    @PostMapping("/v2/product/order/logistics/detail")
    @ApiOperation("查看物流")
    public ResponseEntity<VoViewFindOrderLogisticsDetailRes> findOrderLogisticsDetail(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoFindOrderLogisticsDetail voFindOrderLogisticsDetail) {
        return productOrderBiz.findOrderLogisticsDetail(voFindOrderLogisticsDetail);
    }
}
