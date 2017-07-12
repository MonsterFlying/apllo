package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import com.gofobao.framework.repayment.vo.request.VoInstantlyRepaymentReq;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.repayment.vo.response.VoViewRepaymentOrderDetailWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping
@Slf4j
public class BorrowRepaymentContorller {

    @Autowired
    private RepaymentBiz repaymentBiz;

    @RequestMapping(value = "/repayment/v2/collection/days/{time}", method = RequestMethod.GET)
    @ApiOperation("还款计划-日历控件 time: 201706")
    public ResponseEntity<VoViewCollectionDaysWarpRes> days(@PathVariable String time,
                                                            @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return repaymentBiz.days(userId, time);
    }

    @RequestMapping(value = "/repayment/v2/list/{time}", method = RequestMethod.GET)
    @ApiOperation("还款计划-指定某天还款列表  time:2017-05-02")
    public ResponseEntity<VoViewCollectionOrderListWarpResp> listRes(@PathVariable("time") String time,
                                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoCollectionOrderReq orderReq = new VoCollectionOrderReq();
        orderReq.setTime(time);
        orderReq.setUserId(userId);
        return repaymentBiz.repaymentList(orderReq);
    }

    @RequestMapping(value = "/repayment/v2/detail/{repaymentId}" +
            "" +
            "" +
            "", method = RequestMethod.GET)
    @ApiOperation("还款计划-还款详情")
    public ResponseEntity<VoViewRepaymentOrderDetailWarpRes> info(@PathVariable("repaymentId") String repaymentId,
                                                                  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoInfoReq voInfoReq = new VoInfoReq();
        voInfoReq.setUserId(userId);
        voInfoReq.setRepaymentId(repaymentId);
        return repaymentBiz.detail(voInfoReq);
    }


    /**
     * 立即还款
     *
     * @param voInstantlyRepaymentReq
     * @return 0成功 1失败 2操作不存在 3该借款上一期还未还 4账户余额不足，请先充值
     * @throws Exception
     */
    @PostMapping("/repayment/v2/instantly")
    @ApiOperation("立即还款")
    public ResponseEntity<VoBaseResp> instantly(@ModelAttribute @Valid VoInstantlyRepaymentReq voInstantlyRepaymentReq,
                                                @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        VoRepayReq voRepayReq = new VoRepayReq();
        voRepayReq.setRepaymentId(voInstantlyRepaymentReq.getRepaymentId());
        voRepayReq.setUserId(userId);
        voRepayReq.setInterestPercent(0d);
        voRepayReq.setIsUserOpen(true);
        return repaymentBiz.repay(voRepayReq);
    }

}
