package com.gofobao.framework.tender.controller.web;

import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.response.VoViewTransferMayWarpRes;
import com.gofobao.framework.tender.vo.response.VoViewTransferOfWarpRes;
import com.gofobao.framework.tender.vo.response.VoViewTransferedWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/6/12.
 */
@RestController
@RequestMapping("/transfer/pc")
@Api(description = "债券")
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

    @ApiOperation("转让中列表")
    @GetMapping("v2/transfered/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferedWarpRes> pcTransferedlist(@PathVariable Integer pageIndex,
                                                                    @PathVariable Integer pageSize,
                                                                    @RequestAttribute(SecurityContants.USERID_KEY) Long userId){

        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferedlist(transferReq);
    }
    @ApiOperation("转让中列表")
    @GetMapping("v2/transferMay/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferMayWarpRes> pcTransferMayList(@PathVariable Integer pageIndex,
                                                                      @PathVariable Integer pageSize,
                                                                      @RequestAttribute(SecurityContants.USERID_KEY) Long userId){

        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferMayList(transferReq);
    }


}
