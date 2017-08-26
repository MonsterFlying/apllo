package com.gofobao.framework.repayment.controller.web;

import com.gofobao.framework.borrow.vo.request.VoRepayAllReq;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.*;
import com.gofobao.framework.repayment.vo.response.pc.VoViewCollectionWarpRes;
import com.gofobao.framework.repayment.vo.response.pc.VoViewOrderListWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by admin on 2017/6/1.
 */
@RestController
@Api(description = "pc:还款计划")
@RequestMapping("/repayment/pc")
@Slf4j
public class WebBorrowRepaymentContorller {

    @Autowired
    private RepaymentBiz repaymentBiz;

    @GetMapping(value = "/v2/collection/days/{pageIndex}/{pageSize}")
    @ApiOperation("还款计划列表 ")
    public ResponseEntity<VoViewOrderListWarpRes> days(@PathVariable("pageIndex") Integer pageIndex,
                                                       @PathVariable("pageSize") Integer pageSize,
                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        VoOrderListReq listReq = new VoOrderListReq();
        listReq.setPageIndex(pageIndex);
        listReq.setPageSize(pageSize);
        listReq.setUserId(userId);
        return repaymentBiz.pcRepaymentList(listReq);
    }


    @GetMapping(value = "/v2/collection/days/toExcel")
    @ApiOperation("还款计划列表导出excel ")
    public void days(HttpServletResponse response,
                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoOrderListReq listReq = new VoOrderListReq();
        listReq.setUserId(userId);
        repaymentBiz.toExcel(response, listReq);
    }


    @PostMapping(value = "/v2/order/list")
    @ApiOperation("还款计划列表 time:2017-05-02")
    public ResponseEntity<VoViewCollectionWarpRes> listRes(VoCollectionListReq voCollectionListReq,
                                                           @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voCollectionListReq.setUserId(userId);
        return repaymentBiz.orderList(voCollectionListReq);
    }

    @PostMapping("/pub/pc/repay/all")
    @ApiOperation("提前还款")
    public ResponseEntity<VoBaseResp> pcRepayAll(@Valid @ModelAttribute VoRepayAllReq voRepayAllReq) {
        try {
            return repaymentBiz.pcRepayAll(voRepayAllReq);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "提前还款失败！"));
        }
    }

    /**
     * 立即还款
     *
     * @param voPcRepay
     * @return 0成功 1失败 2操作不存在 3该借款上一期还未还 4账户余额不足，请先充值
     * @throws Exception
     */
    @PostMapping("/v2/pcInstantly")
    @ApiOperation("立即还款")
    public ResponseEntity<VoBaseResp> pcInstantly(@ModelAttribute @Valid VoPcRepay voPcRepay) throws Exception {
        try {
            return repaymentBiz.pcInstantly(voPcRepay);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "提前还款失败！"));
        }
    }


    /**
     * 垫付
     *
     * @param voPcAdvanceReq
     * @return
     */
    @PostMapping("/pub/v2/advance")
    @ApiOperation("垫付")
    public ResponseEntity<VoBaseResp> pcAdvance(@ModelAttribute @Valid VoPcAdvanceReq voPcAdvanceReq) {
        try {
            return repaymentBiz.pcAdvance(voPcAdvanceReq);
        } catch (Throwable e) {
            log.error("垫付异常:", e);
        }
        return ResponseEntity
                .badRequest()
                .body(VoBaseResp.error(VoBaseResp.ERROR, "垫付失败!"));
    }
}
