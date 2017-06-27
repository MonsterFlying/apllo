package com.gofobao.framework.collection.controller;

import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
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
@Api(description = "回款明细")
@RestController
@RequestMapping("pub/payment")
public class PaymentController {

    @Autowired
    private PaymentBiz paymentBiz;

    @ApiOperation("回款期数列表 time:2017-05-06")
    @GetMapping("/v2/order/list/{time}")
    public ResponseEntity<VoViewCollectionOrderListWarpResp> collectionOrderList(@PathVariable("time") String time/*,
                                                          @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        VoCollectionOrderReq voCollectionOrderReq = new VoCollectionOrderReq();
        voCollectionOrderReq.setUserId(901L);
        voCollectionOrderReq.setTime(time);
        return paymentBiz.orderList(voCollectionOrderReq);
    }

    @ApiOperation("回款详情")
    @GetMapping("/v2/order/detail/{collectionId}")
    public ResponseEntity<VoViewOrderDetailWarpRes> orderDetail(@PathVariable("collectionId") Long collectionId/*,
                                                                @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        VoOrderDetailReq voOrderDetailReq = new VoOrderDetailReq();
        voOrderDetailReq.setCollectionId(collectionId);
        voOrderDetailReq.setUserId(901L);
        return paymentBiz.orderDetail(voOrderDetailReq);
    }

    @ApiOperation("当月有回款日期,time：'201705'")
    @GetMapping("/v2/collection/days/{time}")
    public ResponseEntity<VoViewCollectionDaysWarpRes> days(@PathVariable("time") String time/*,
                                                            @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        Long userId=901L;

        return paymentBiz.collectionDays(time, userId);
    }


}
