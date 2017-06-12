package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListResWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by admin on 2017/6/1.
 */
@RestController
@Api(description = "还款计划")
@RequestMapping("/repayment")
public class BorrowRepaymentContorller {

    @Autowired
    private RepaymentBiz repaymentBiz;

    @RequestMapping(value = "/v2/list/{time}", method = RequestMethod.GET)
    @ApiOperation("还款计划列表")
    public ResponseEntity<VoViewCollectionOrderListResWarpRes> listRes(@PathVariable("time") String time,
                                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCollectionOrderReq orderReq = new VoCollectionOrderReq();
        orderReq.setTime(time);
        orderReq.setUserId(userId);
        return repaymentBiz.repaymentList(orderReq);
    }

    @RequestMapping(value = "/v2/info/{repaymentId}", method = RequestMethod.GET)
    @ApiOperation("还款信息")
    public ResponseEntity<VoViewOrderDetailWarpRes> info(@PathVariable("repaymentId") String repaymentId,
                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoInfoReq voInfoReq = new VoInfoReq();
        voInfoReq.setUserId(userId);
        voInfoReq.setRepaymentId(repaymentId);
        return repaymentBiz.info(voInfoReq);
    }

    /**
     * 立即还款
     *
     * @param voInstantlyRepaymentReq
     * @return 0成功 1失败 2操作不存在 3该借款上一期还未还 4账户余额不足，请先充值
     * @throws Exception
     */
    @RequestMapping("/v2/instantly")
    @ApiOperation("立即还款")
    public ResponseEntity<VoBaseResp> instantly(@ModelAttribute @Valid VoInstantlyRepaymentReq voInstantlyRepaymentReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voInstantlyRepaymentReq.setUserId(userId);
        return repaymentBiz.instantly(voInstantlyRepaymentReq);
    }
}
