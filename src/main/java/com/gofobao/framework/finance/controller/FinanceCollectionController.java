package com.gofobao.framework.finance.controller;

import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import com.gofobao.framework.finance.biz.FinanceCollectionBiz;
import com.gofobao.framework.finance.vo.request.VoFinanceCollectionDetailReq;
import com.gofobao.framework.finance.vo.response.VoViewFinanceCollectionDetailResp;
import com.gofobao.framework.finance.vo.response.VoViewFinanceCollectionListResp;
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
@Api(description = "理财计划回款明细")
@RestController
@RequestMapping("/finance/collection")
public class FinanceCollectionController {

    @Autowired
    private FinanceCollectionBiz financeCollectionBiz;

    @ApiOperation("回款明细-回款列表 time:2017-05-06")
    @GetMapping("/v2/order/list/{time}")
    public ResponseEntity<VoViewFinanceCollectionListResp> collectionOrderList(@PathVariable("time") String time,
                                                                               @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCollectionOrderReq voCollectionOrderReq = new VoCollectionOrderReq();
        voCollectionOrderReq.setUserId(userId);
        voCollectionOrderReq.setTime(time);
        return financeCollectionBiz.orderList(voCollectionOrderReq);
    }

    @ApiOperation("回款明细-回款详情")
    @GetMapping("/v2/order/detail/{collectionId}")
    public ResponseEntity<VoViewFinanceCollectionDetailResp> orderDetail(@PathVariable("collectionId") Long collectionId,
                                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoFinanceCollectionDetailReq voOrderDetailReq = new VoFinanceCollectionDetailReq();
        voOrderDetailReq.setCollectionId(collectionId);
        voOrderDetailReq.setUserId(userId);
        return financeCollectionBiz.orderDetail(voOrderDetailReq);
    }

    @ApiOperation("回款明细-日历控件,time：'201705'")
    @GetMapping("/v2/collection/days/{time}")
    public ResponseEntity<VoViewCollectionDaysWarpRes> days(@PathVariable("time") String time,
                                                            @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return financeCollectionBiz.collectionDays(time, userId);
    }


}
