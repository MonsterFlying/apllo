package com.gofobao.framework.tender.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.request.VoTransferTenderReq;
import com.gofobao.framework.tender.vo.response.VoGoTenderInfo;
import com.gofobao.framework.tender.vo.response.VoViewTransferMayWarpRes;
import com.gofobao.framework.tender.vo.response.VoViewTransferOfWarpRes;
import com.gofobao.framework.tender.vo.response.VoViewTransferedWarpRes;
import com.gofobao.framework.tender.vo.response.web.VoViewTransferBuyWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by admin on 2017/6/12.
 */
@RestController
@RequestMapping("/transfer/pc")
@Api(description = "pc:债券")
public class WebTransferController {

    @Autowired
    private TransferBiz transferBiz;
    VoTransferReq transferReq=new VoTransferReq();

    @ApiOperation("转让中列表")
    @GetMapping("v2/transferOf/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(@PathVariable Integer pageIndex,
                                                                 @PathVariable Integer pageSize,
                                                                 @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.tranferOfList(transferReq);
    }

    @ApiOperation("已转让列表")
    @GetMapping("v2/transfered/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferedWarpRes> pcTransferedlist(@PathVariable Integer pageIndex,
                                                                    @PathVariable Integer pageSize,
                                                                    @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferedlist(transferReq);
    }
    @ApiOperation("可转让列表")
    @GetMapping("v2/transferMay/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferMayWarpRes> pcTransferMayList(@PathVariable Integer pageIndex,
                                                                      @PathVariable Integer pageSize,
                                                                      @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferMayList(transferReq);
    }


    @ApiOperation("已购买的债权")
    @GetMapping("v2/transferBuy/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferBuyWarpRes> pcTransferBuy(@PathVariable Integer pageIndex,
                                                                      @PathVariable Integer pageSize,
                                                                      @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.tranferBuyList(transferReq);
    }

    /**
     * 债权转让
     *
     * @param voTransferTenderReq
     * @return
     */
    @ApiOperation("债权转让")
    @PostMapping("v2/transfer")
    public ResponseEntity<VoBaseResp> pcTransferTender(@ModelAttribute @Valid VoTransferTenderReq voTransferTenderReq,
                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voTransferTenderReq.setUserId(userId);
        return transferBiz.transferTender(voTransferTenderReq);
    }

    /**
     * 获取立即转让详情
     *
     * @param tenderId 投标记录Id
     * @return
     */
    @ApiOperation("获取立即转让详情")
    @GetMapping("v2/transfer/info/{tenderId}")
    public ResponseEntity<VoGoTenderInfo> pcGoTenderInfo(@PathVariable Long tenderId,
                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return transferBiz.goTenderInfo(tenderId,userId);
    }
}
