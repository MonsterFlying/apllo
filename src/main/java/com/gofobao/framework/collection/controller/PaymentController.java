package com.gofobao.framework.collection.controller;

import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by admin on 2017/5/31.
 */
@ApiModel("回款明细")
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @ApiOperation("回款期数列表")
    @GetMapping("/order/list/{time}")
    public ResponseEntity<VoViewCollectionOrderListRes> collectionOrderList(@PathVariable("time") String time,
                                                                            @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCollectionOrderReq voCollectionOrderReq = new VoCollectionOrderReq();
        voCollectionOrderReq.setUserId(userId);
        voCollectionOrderReq.setTime(time);
        VoViewCollectionOrderListRes collectionOrderListRes = new VoViewCollectionOrderListRes();
        try {
            collectionOrderListRes = borrowCollectionService.orderList(voCollectionOrderReq);
            ResponseEntity.status(HttpStatus.OK);
            ResponseEntity.ok(collectionOrderListRes);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(collectionOrderListRes);
    }

    @ApiOperation("回款详情")
    @GetMapping("/order/detail/{collectionId}")
    public ResponseEntity<VoViewOrderDetailRes> orderDetail(@PathVariable("collectionId") Long collectionId,
                                                            @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoOrderDetailReq voOrderDetailReq = new VoOrderDetailReq();
        voOrderDetailReq.setCollectionId(collectionId);
        voOrderDetailReq.setUserId(userId);
        VoViewOrderDetailRes voViewOrderDetailRes = new VoViewOrderDetailRes();
        try {
            voViewOrderDetailRes = borrowCollectionService.orderDetail(voOrderDetailReq);
            ResponseEntity.status(HttpStatus.OK);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(voViewOrderDetailRes);
    }






}
