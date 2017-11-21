package com.gofobao.framework.product.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.biz.ProductBiz;
import com.gofobao.framework.product.vo.request.*;
import com.gofobao.framework.product.vo.response.*;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/11/10.
 */
@RestController
@RequestMapping("/finance")
@Slf4j
@Api(description = "商品控制器")
public class FinanceProductController {

    @Autowired
    private ProductBiz productBiz;

    /**
     * 取消订单
     */
    @PostMapping("/v2/product/order/cancel")
    @ApiOperation("取消订单")
    public ResponseEntity<VoBaseResp> cancelOrder(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoCancelOrder voCancelOrder) {
        voCancelOrder.setUserId(userId);
        try {
            return productBiz.cancelOrder(voCancelOrder);
        } catch (Exception e) {
            log.error("广富送取消订单失败：", e);
            return ResponseEntity.ok(VoBaseResp.ok("取消订单失败!"));
        }
    }

    /**
     * 我的订单列表页面
     */
    @PostMapping("/v2/product/order/list")
    @ApiOperation("我的订单列表页面")
    public ResponseEntity<VoViewProductOrderListRes> findProductOrderList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoFindProductOrderList voFindProductOrderList) {
        voFindProductOrderList.setUserId(userId);
        return productBiz.findProductOrderList(voFindProductOrderList);
    }

    /**
     * 订单详情页面
     */
    @PostMapping("/v2/product/order/detail")
    @ApiOperation("订单详情页面")
    public ResponseEntity<VoViewProductOrderDetailRes> findProductOrderDetail(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoFindProductOrderDetail voFindProductOrderDetail) {
        voFindProductOrderDetail.setUserId(userId);
        return productBiz.findProductOrderDetail(voFindProductOrderDetail);
    }

    /**
     * 付款接口
     */
    @PostMapping("/v2/product/pay")
    @ApiOperation("付款接口")
    public ResponseEntity<VoBaseResp> orderPay(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoFindOrderPay voFindOrderPay) {
        voFindOrderPay.setUserId(userId);
        try {
            return productBiz.orderPay(voFindOrderPay);
        } catch (Exception e) {
            log.error("广富送付款失败：", e);
            return ResponseEntity.ok(VoBaseResp.ok("支付失败!"));
        }
    }

    /**
     * 立即购买
     */
    @PostMapping("/v2/product/buy")
    @ApiOperation("立即购买")
    public ResponseEntity<VoViewBuyProductPlanRes> buyProductPlan(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoBuyProductPlan voBuyProductPlan) {
        voBuyProductPlan.setUserId(userId);
        try {
            return productBiz.buyProductPlan(voBuyProductPlan);
        } catch (Exception e) {
            log.error("广富送立即购买失败：", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "购买失败！", VoViewBuyProductPlanRes.class));
        }
    }

    /**
     * 下单页面
     */
    @PostMapping("/v2/product/bought")
    @ApiOperation("下单页面")
    public ResponseEntity<VoViewBoughtProductPlanRes> boughtProductPlan(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoBoughtProductPlan voBoughtProductPlan) {
        voBoughtProductPlan.setUserId(userId);
        return productBiz.boughtProductPlan(voBoughtProductPlan);
    }

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


    /**
     * 查询广富送商品详情
     *
     * @param voFindProductDetail
     * @return
     */
    @PostMapping("/pub/v2/product/detail")
    @ApiOperation("查询广富送商品详情")
    public ResponseEntity<VoViewFindProductItemDetailsRes> findProductDetail(@Valid @ModelAttribute VoFindProductDetail voFindProductDetail) {
        return productBiz.findProductDetail(voFindProductDetail);
    }
}
