package com.gofobao.framework.collection.controller;

import com.gofobao.framework.borrow.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.borrow.vo.response.VoViewCollectionOrderListRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by admin on 2017/5/31.
 */
@ApiModel("回款明细")
@RestController
@RequestMapping("/payment")
public class PaymentCollection {



    @ApiOperation("回款期数列表")
    @GetMapping("/order/list/{time}")
    public ResponseEntity<VoViewCollectionOrderListRes> collectionOrderList(@PathVariable("time") String time, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCollectionOrderReq voCollectionOrderReq = new VoCollectionOrderReq();
        voCollectionOrderReq.setUserId(userId);
        voCollectionOrderReq.setTime(time);

        return null;

    }

}
