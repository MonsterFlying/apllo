package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
/**
 * Created by admin on 2017/6/1.
 */
@RestController
@RequestMapping("/repayment")
public class BorrowRepaymentContorller {

    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    @RequestMapping("/v2/list/{time}")
    @ApiOperation("还款计划列表")
    public ResponseEntity<VoViewCollectionOrderListRes> listRes(@PathVariable("time") String time,
                                                                @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCollectionOrderReq orderReq = new VoCollectionOrderReq();
        orderReq.setTime(time);
        orderReq.setUserId(userId);
        try {
            VoViewCollectionOrderListRes listRes = borrowRepaymentService.repaymentList(orderReq);
            ResponseEntity.status(HttpStatus.OK);
            return ResponseEntity.ok(listRes);

        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
            return ResponseEntity.ok(null);
        }
    }

    @RequestMapping("/v2/info/{repaymentId}")
    @ApiOperation("还款计划列表")
    public ResponseEntity<VoViewOrderDetailRes> info(@PathVariable("repaymentId") String repaymentId,
                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoInfoReq voInfoReq = new VoInfoReq();
        voInfoReq.setUserId(userId);
        voInfoReq.setRepaymentId(repaymentId);
        try {
            VoViewOrderDetailRes voViewOrderDetailRes = borrowRepaymentService.info(voInfoReq);
            ResponseEntity.status(HttpStatus.OK);
            return  new ResponseEntity<>(voViewOrderDetailRes,HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return  new ResponseEntity<>(new VoViewOrderDetailRes(),HttpStatus.BAD_REQUEST);
        }
    }




}
