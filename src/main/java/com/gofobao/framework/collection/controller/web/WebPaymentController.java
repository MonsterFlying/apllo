package com.gofobao.framework.collection.controller.web;

import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.vo.request.OrderListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.response.web.VoCollectionListByDays;
import com.gofobao.framework.collection.vo.response.web.VoViewCollectionListWarpRes;
import com.gofobao.framework.collection.vo.response.web.VoViewCollectionWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;

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
    public ResponseEntity<VoViewCollectionWarpRes> collectionOrderList(VoCollectionListReq collectionListReq,
                                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        collectionListReq.setUserId(901L);
        return paymentBiz.pcOrderDetail(collectionListReq);
    }


    @ApiOperation("回款明细")
    @GetMapping("/v2/days/collection/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewCollectionListWarpRes> orderDetail(@PathVariable("pageIndex") Integer pageIndex,
                                                                   @PathVariable("pageSize") Integer pageSize,
                                                                   @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        OrderListReq orderListReq = new OrderListReq();
        orderListReq.setUserId(userId);
        orderListReq.setPageIndex(pageIndex);
        orderListReq.setPageSize(pageSize);
        return paymentBiz.pcOrderList(orderListReq);
    }


    @ApiOperation("回款明细导出Excel")
    @GetMapping("/v2/days/collection/toExcel")
    public void orderDetail(HttpServletResponse response,
                            @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        OrderListReq orderListReq = new OrderListReq();
        orderListReq.setUserId(userId);
        paymentBiz.toExcel(response, orderListReq);
    }


    /**
     * 根据时间查询回款列表
     *
     * @param date
     * @param userId
     * @return
     */
    @ApiOperation("根据时间查询回款列表")
    @GetMapping("/v2/days/collection/list/{date}")
    public ResponseEntity<VoCollectionListByDays> collectionListByDays(@PathVariable("date") String date,
                                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return paymentBiz.collectionListByDays(date, userId);
    }


}
