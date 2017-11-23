package com.gofobao.framework.product.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.vo.request.*;
import com.gofobao.framework.product.vo.response.*;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/11/10.
 */
public interface ProductBiz {

    /**
     * 删除订单
     */
    ResponseEntity<VoBaseResp> delOrder(VoDelOrder voDelOrder) throws Exception;

    /**
     * 取消订单
     */
    ResponseEntity<VoBaseResp> cancelOrder(VoCancelOrder voCancelOrder) throws Exception;

    /**
     * 付款接口
     */
    ResponseEntity<VoBaseResp> orderPay(VoFindOrderPay voFindOrderPay) throws Exception;

    /**
     * 订单详情页面
     */
    ResponseEntity<VoViewProductOrderDetailRes> findProductOrderDetail(VoFindProductOrderDetail voFindProductOrderDetail);

    /**
     * 我的订单列表页面
     */
    ResponseEntity<VoViewProductOrderListRes> findProductOrderList(VoFindProductOrderList voFindProductOrderList);

    /**
     * 下单页面
     */
    ResponseEntity<VoViewBoughtProductPlanRes> boughtProductPlan(VoBoughtProductPlan voBoughtProductPlan);

    /**
     * 立即购买
     */
    ResponseEntity<VoViewBuyProductPlanRes> buyProductPlan(VoBuyProductPlan voBuyProductPlan) throws Exception;

    /**
     * 查询广富送商品列表
     */
    ResponseEntity<VoViewFindProductPlanListRes> findProductPlanList(VoFindProductPlanList voFindProductPlanList);

    /**
     * 查询广富送商品详情
     *
     * @param voFindProductDetail
     * @return
     */
    ResponseEntity<VoViewFindProductItemDetailsRes> findProductDetail(VoFindProductDetail voFindProductDetail);

    /**
     * 查询广富送计划详情
     *
     * @param voFindProductPlanDetail
     * @return
     */
    ResponseEntity<VoViewFindProductPlanDetailRes> findProductPlanDetail(VoFindProductPlanDetail voFindProductPlanDetail);
}
