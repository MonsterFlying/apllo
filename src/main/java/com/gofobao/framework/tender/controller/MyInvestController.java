package com.gofobao.framework.tender.controller;

import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.service.InvestService;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.request.VoViewReturnedMoney;
import com.gofobao.framework.tender.vo.response.VoViewBackMoney;
import com.gofobao.framework.tender.vo.response.VoViewBiddingRes;
import com.gofobao.framework.tender.vo.response.VoViewSettleRes;
import com.gofobao.framework.tender.vo.response.VoViewTenderDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Created by admin on 2017/6/1.
 */
@RequestMapping("/invest")
@ApiModel("我的投资")
@RestController
public class MyInvestController {

    @Autowired
    private InvestService investService;

    /**
     * 回款中列表
     *
     * @return
     */
    @ApiOperation("回款中列表")
    @GetMapping("/v2/backMoney/list")
    public ResponseEntity<List<VoViewBackMoney>> backMoneyList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        try {
            List<VoViewBackMoney> backMoneyList = investService.backMoneyList(voInvestListReq);
            ResponseEntity.status(HttpStatus.OK);
            return ResponseEntity.ok(backMoneyList);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);

        }
        return null;
    }


    @ApiOperation("投标中列表")
    @GetMapping("/v2/biddingList/list")
    public ResponseEntity<List<VoViewBiddingRes>> biddingList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        try {
            List<VoViewBiddingRes> backMoneyList = investService.biddingList(voInvestListReq);
            ResponseEntity.status(HttpStatus.OK);
            return ResponseEntity.ok(backMoneyList);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return null;
    }


    @ApiOperation("已结清列表")
    @GetMapping("/v2/biddingList/list")
    public ResponseEntity<List<VoViewSettleRes>> settleList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        try {
            List<VoViewSettleRes> backMoneyList = investService.settleList(voInvestListReq);
            ResponseEntity.status(HttpStatus.OK);
            return ResponseEntity.ok(backMoneyList);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return null;
    }


    @ApiOperation("投资详情")
    @GetMapping("/v2/tender/detail/{tenderId}")
    public ResponseEntity<VoViewTenderDetail> tenderDetail(@PathVariable Long tenderId, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq=new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setTenderId(tenderId);

        try {
            VoViewTenderDetail viewTenderDetail= investService.tenderDetail(voDetailReq);
            ResponseEntity.status(HttpStatus.OK);
            return ResponseEntity.ok(viewTenderDetail);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    @ApiOperation("投资详情")
    @GetMapping("/v2/tender/collection/{tenderId}")
    public ResponseEntity<VoViewReturnedMoney> infoList(@PathVariable Long tenderId, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq=new VoDetailReq();
        voDetailReq.setTenderId(tenderId);
        voDetailReq.setUserId(userId);
        try {
            VoViewReturnedMoney viewTenderDetail= investService.infoList(voDetailReq);
            ResponseEntity.status(HttpStatus.OK);
            return ResponseEntity.ok(viewTenderDetail);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return null;
    }


}
