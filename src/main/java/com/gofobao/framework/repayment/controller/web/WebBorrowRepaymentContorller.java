package com.gofobao.framework.repayment.controller.web;

import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.project.SecurityHelper;
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

import javax.validation.Valid;
import java.util.Date;
import java.util.Map;

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

    @GetMapping(value = "/v2/collection/days")
    @ApiOperation("还款计划列表 ")
    public ResponseEntity<VoViewOrderListWarpRes> days( VoOrderListReq listReq,
                                                            @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        listReq.setUserId(userId);
        return repaymentBiz.pcRepaymentList(listReq);
    }

    @PostMapping(value = "/v2/order/list")
    @ApiOperation("还款计划列表 time:2017-05-02")
    public ResponseEntity<VoViewCollectionWarpRes> listRes(VoCollectionListReq voCollectionListReq,
                                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voCollectionListReq.setUserId(userId);
        return repaymentBiz.orderList(voCollectionListReq);
    }


    /**
     * 立即还款
     *
     * @param voPcInstantlyRepaymentReq
     * @return 0成功 1失败 2操作不存在 3该借款上一期还未还 4账户余额不足，请先充值
     * @throws Exception
     */
    @PostMapping("/v2/instantly")
    @ApiOperation("立即还款")
    public ResponseEntity<VoBaseResp> pcInstantly(@ModelAttribute @Valid VoPcInstantlyRepaymentReq voPcInstantlyRepaymentReq) throws Exception {
        return repaymentBiz.pcRepay(voPcInstantlyRepaymentReq);
    }


    /**
     * 垫付
     *
     * @param voPcAdvanceReq
     * @return
     */
    @PostMapping("/v2/advance")
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
