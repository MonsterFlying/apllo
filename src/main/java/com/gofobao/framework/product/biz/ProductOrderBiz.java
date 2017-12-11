package com.gofobao.framework.product.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.vo.request.*;
import com.gofobao.framework.product.vo.response.VoViewBoughtProductPlanRes;
import com.gofobao.framework.product.vo.response.VoViewFindOrderLogisticsDetailRes;
import com.gofobao.framework.product.vo.response.VoViewProductOrderDetailRes;
import com.gofobao.framework.product.vo.response.VoViewProductOrderListRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/11/22.
 */
public interface ProductOrderBiz {

    /**
     * 订单审核
     * @param voAuditOrder
     * @return
     */
    ResponseEntity<VoBaseResp> auditOrder(VoAuditOrder voAuditOrder);

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
     * 查看物流
     */
    ResponseEntity<VoViewFindOrderLogisticsDetailRes> findOrderLogisticsDetail(VoFindOrderLogisticsDetail voFindOrderLogisticsDetail);

/*    *//**
     * 确认收货
     *//*
    ResponseEntity<VoViewFindOrderLogisticsDetailRes> (VoFindOrderLogisticsDetail voFindOrderLogisticsDetail);*/
}
