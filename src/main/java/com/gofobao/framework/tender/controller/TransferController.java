package com.gofobao.framework.tender.controller;

import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.response.VoViewTransferMayWarpRes;
import com.gofobao.framework.tender.vo.response.VoViewTransferOfWarpRes;
import com.gofobao.framework.tender.vo.response.VoViewTransferedWarpRes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/12.
 */
@RestController
@RequestMapping("/transfer")
@ApiModel("债券")
public class TransferController {

    @Autowired
    private TransferBiz transferBiz;


    @ApiOperation("转让中列表")
    @GetMapping("v2/transferOf/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(@PathVariable Integer pageIndex,@PathVariable Integer pageSize){

        VoTransferReq transferReq=new VoTransferReq();
        transferReq.setUserId(901L);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.tranferOfList(transferReq);
    }

    @ApiOperation("转让中列表")
    @GetMapping("v2/transfered/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferedWarpRes> transferedlist(@PathVariable Integer pageIndex, @PathVariable Integer pageSize){

        VoTransferReq transferReq=new VoTransferReq();
        transferReq.setUserId(901L);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferedlist(transferReq);
    }
    @ApiOperation("转让中列表")
    @GetMapping("v2/transferMay/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferMayWarpRes> transferMayList(@PathVariable Integer pageIndex, @PathVariable Integer pageSize){
        VoTransferReq transferReq=new VoTransferReq();
        transferReq.setUserId(901L);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferMayList(transferReq);
    }


}
