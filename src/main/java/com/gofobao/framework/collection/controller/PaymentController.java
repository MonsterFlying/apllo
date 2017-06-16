package com.gofobao.framework.collection.controller;

import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListResWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by admin on 2017/5/31.
 */
@Api(description="回款明细")
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentBiz paymentBiz;

    @ApiOperation("回款期数列表 time:2017-05-06")
    @GetMapping("/v2/order/list/{time}")
    public ResponseEntity<VoViewCollectionOrderListResWarpRes> collectionOrderList(@PathVariable("time") String time,
                                                                                   @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCollectionOrderReq voCollectionOrderReq = new VoCollectionOrderReq();
        voCollectionOrderReq.setUserId(userId);
        voCollectionOrderReq.setTime(time);
        return paymentBiz.orderList(voCollectionOrderReq);
    }

    @ApiOperation("回款详情")
    @GetMapping("/v2/order/detail/{collectionId}")
    public ResponseEntity<VoViewOrderDetailWarpRes> orderDetail(@PathVariable("collectionId") Long collectionId,
                                                                @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoOrderDetailReq voOrderDetailReq = new VoOrderDetailReq();
        voOrderDetailReq.setCollectionId(collectionId);
        voOrderDetailReq.setUserId(userId);

        return paymentBiz.orderDetail(voOrderDetailReq);
    }

    @ApiOperation("当月有回款日期,time：'201705'")
    @GetMapping("/v2/collection/days/{time}")
    public ResponseEntity<VoViewCollectionDaysWarpRes> days(@PathVariable("time") String time,
                                                            @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        return paymentBiz.collectionDays(time,userId);
    }



}
