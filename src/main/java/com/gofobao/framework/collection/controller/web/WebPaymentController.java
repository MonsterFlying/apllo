package com.gofobao.framework.collection.controller.web;

import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.vo.request.OrderListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.response.web.VoViewCollectionListWarpRes;
import com.gofobao.framework.collection.vo.response.web.VoViewCollectionWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/5/31.
 */
@Api(description = "pc:回款明细")
@RestController
@RequestMapping("payment/pc")
public class WebPaymentController {

    @Autowired
    private PaymentBiz paymentBiz;

    @ApiOperation("回款明细-回款详情 time:2017-05-06")
    @GetMapping("/v2/collection/list")
    public ResponseEntity<VoViewCollectionWarpRes> collectionOrderList(VoCollectionListReq collectionListReq/*,
                                                                                 @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        collectionListReq.setUserId(901L);
        return paymentBiz.pcOrderDetail(collectionListReq);
    }


    @ApiOperation("回款明细")
    @GetMapping("/v2/days/collection/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewCollectionListWarpRes> orderDetail(@PathVariable("pageIndex") Integer pageIndex,
                                                             @PathVariable("pageSize") Integer pageSize/*,
                                                             @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        OrderListReq orderListReq=new OrderListReq();
        orderListReq.setUserId(901L);
        orderListReq.setPageIndex(pageIndex);
        orderListReq.setPageSize(pageSize);
        return paymentBiz.pcOrderList(orderListReq);
    }



}
