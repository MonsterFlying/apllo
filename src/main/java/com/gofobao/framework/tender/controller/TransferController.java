package com.gofobao.framework.tender.controller;

import com.gofobao.framework.helper.ThymeleafHelper;
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
@RequestMapping("/transfer")
@Api(description = "债券")
public class TransferController {

    @Autowired
    private TransferBiz transferBiz;

    @Autowired
    private ThymeleafHelper thymeleafHelper;


    @ApiOperation("转让中列表")
    @GetMapping("v2/transferOf/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable Integer pageIndex, @PathVariable Integer pageSize) {

        VoTransferReq transferReq = new VoTransferReq();
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.tranferOfList(transferReq);
    }

    @ApiOperation("转让中列表")
    @GetMapping("v2/transfered/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferedWarpRes> transferedlist(@RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable Integer pageIndex, @PathVariable Integer pageSize) {

        VoTransferReq transferReq = new VoTransferReq();
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferedlist(transferReq);
    }

    @ApiOperation("转让中列表")
    @GetMapping("v2/transferMay/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferMayWarpRes> transferMayList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable Integer pageIndex, @PathVariable Integer pageSize) {
        VoTransferReq transferReq = new VoTransferReq();
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferMayList(transferReq);
    }


    @ApiOperation("债券转让说明")
    @PostMapping("/v2/transfer/desc")
    public ResponseEntity<String> desc() {
        String content;
        try {
            content = thymeleafHelper.build("tender/translate", null);
        } catch (Exception e) {
            content = thymeleafHelper.build("load_error", null);
        }
        return ResponseEntity.ok(content);
    }
}
