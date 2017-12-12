package com.gofobao.framework.product.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.biz.ProductCollectBiz;
import com.gofobao.framework.product.biz.ProductOrderBiz;
import com.gofobao.framework.product.vo.request.*;
import com.gofobao.framework.product.vo.response.VoViewFindOrderLogisticsDetailRes;
import com.gofobao.framework.product.vo.response.VoViewFindProductCollectListRes;
import com.gofobao.framework.product.vo.response.VoViewProductOrderDetailRes;
import com.gofobao.framework.product.vo.response.VoViewProductOrderListRes;
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
     * 删除订单
     */
    @PostMapping("/v2/product/order/del")
    @ApiOperation("删除订单")
    public ResponseEntity<VoBaseResp> delOrder(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoDelOrder voDelOrder) {
        voDelOrder.setUserId(userId);
        try {
            return productOrderBiz.delOrder(voDelOrder);
        } catch (Exception e) {
            log.error("广富送删除订单失败：", e);
            return ResponseEntity.ok(VoBaseResp.ok("删除订单失败!"));
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/v2/product/order/cancel")
    @ApiOperation("取消订单")
    public ResponseEntity<VoBaseResp> cancelOrder(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoCancelOrder voCancelOrder) {
        voCancelOrder.setUserId(userId);
        try {
            return productOrderBiz.cancelOrder(voCancelOrder);
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
        return productOrderBiz.findProductOrderList(voFindProductOrderList);
    }

    /**
     * 订单详情页面
     */
    @PostMapping("/v2/product/order/detail")
    @ApiOperation("订单详情页面")
    public ResponseEntity<VoViewProductOrderDetailRes> findProductOrderDetail(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoFindProductOrderDetail voFindProductOrderDetail) {
        voFindProductOrderDetail.setUserId(userId);
        return productOrderBiz.findProductOrderDetail(voFindProductOrderDetail);
    }

    /**
     * 付款接口
     */
    @PostMapping("/v2/product/pay")
    @ApiOperation("付款接口")
    public ResponseEntity<VoBaseResp> orderPay(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoFindOrderPay voFindOrderPay) {
        voFindOrderPay.setUserId(userId);
        try {
            return productOrderBiz.orderPay(voFindOrderPay);
        } catch (Exception e) {
            log.error("广富送付款失败：", e);
            return ResponseEntity.ok(VoBaseResp.ok("支付失败!"));
        }
    }

    /**
     * 查看物流
     */
    @PostMapping("/v2/product/order/logistics/detail")
    @ApiOperation("查看物流")
    public ResponseEntity<VoViewFindOrderLogisticsDetailRes> findOrderLogisticsDetail(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoFindOrderLogisticsDetail voFindOrderLogisticsDetail) {
        voFindOrderLogisticsDetail.setUserId(userId);
        return productOrderBiz.findOrderLogisticsDetail(voFindOrderLogisticsDetail);
    }

    /**
     * 确认收货
     */
    @PostMapping("/v2/product/order/confirm/receipt")
    @ApiOperation("确认收货")
    public ResponseEntity<VoBaseResp> confirmReceipt(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoConfirmReceipt voConfirmReceipt) {
        voConfirmReceipt.setUserId(userId);
        return productOrderBiz.confirmReceipt(voConfirmReceipt);
    }
}
