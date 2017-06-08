package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListResWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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
@Api(description="还款计划")
@RequestMapping("/repayment")
public class BorrowRepaymentContorller {

    @Autowired
    private RepaymentBiz repaymentBiz;

    @RequestMapping("/v2/list/{time}")
    @ApiOperation("还款计划列表")
    public ResponseEntity<VoViewCollectionOrderListResWarpRes> listRes(@PathVariable("time") String time,
                                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCollectionOrderReq orderReq = new VoCollectionOrderReq();
        orderReq.setTime(time);
        orderReq.setUserId(userId);
        return repaymentBiz.repaymentList(orderReq);
    }

    @RequestMapping("/v2/info/{repaymentId}")
    @ApiOperation("还款计划列表")
    public ResponseEntity<VoViewOrderDetailWarpRes> info(@PathVariable("repaymentId") String repaymentId,
                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoInfoReq voInfoReq = new VoInfoReq();
        voInfoReq.setUserId(userId);
        voInfoReq.setRepaymentId(repaymentId);
        return repaymentBiz.info(voInfoReq);

    }

}
