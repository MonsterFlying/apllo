package com.gofobao.framework.tender.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.vo.request.VoBuyTransfer;
import com.gofobao.framework.tender.vo.request.VoEndTransfer;
import com.gofobao.framework.tender.vo.request.VoTransferReq;
import com.gofobao.framework.tender.vo.request.VoTransferTenderReq;
import com.gofobao.framework.tender.vo.response.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by admin on 2017/6/12.
 */
@RestController
@RequestMapping("pub/transfer")
@Api(description = "债权相关控制器")
@Slf4j
public class TransferController {

    @Autowired
    private TransferBiz transferBiz;

    @Autowired
    private ThymeleafHelper thymeleafHelper;

    /**
     * 结束债权转让
     *
     * @param voEndTransfer
     * @return
     * @throws Exception
     */
    @ApiOperation("新版发布债权转让")
    @GetMapping("v2/new/transfer/end")
    public ResponseEntity<VoBaseResp> endTransfer(@Valid VoEndTransfer voEndTransfer) {
        try {
            return transferBiz.endTransfer(voEndTransfer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后重试!"));
        }
    }

    /**
     * 新版发布债权转让
     *
     * @param voTransferTenderReq
     * @return
     * @throws Exception
     */
    @ApiOperation("新版发布债权转让")
    @GetMapping("v2/new/transfer/publish")
    public ResponseEntity<VoBaseResp> newTransferTender(@Valid VoTransferTenderReq voTransferTenderReq) {
        try {
            return transferBiz.newTransferTender(voTransferTenderReq);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后重试!"));
        }
    }

    /**
     * 购买债权转让
     */
    @ApiOperation("新版购买债权转让")
    @PostMapping("v2/new/transfer/buy")
    public ResponseEntity<VoBaseResp> buyTransfer(@Valid VoBuyTransfer voBuyTransfer, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        try {
            voBuyTransfer.setUserId(userId);
            return transferBiz.buyTransfer(voBuyTransfer);
        } catch (Exception e) {
            log.error("购买债权转让异常：", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后重试!"));
        }
    }

    @ApiOperation("转让中列表")
    @GetMapping("v2/transferOf/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferOfWarpRes> tranferOfList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                 @PathVariable Integer pageIndex,
                                                                 @PathVariable Integer pageSize) {

        VoTransferReq transferReq = new VoTransferReq();
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.tranferOfList(transferReq);
    }

    @ApiOperation("已转让列表")
    @GetMapping("v2/transfered/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferedWarpRes> transferedlist(@PathVariable Integer pageIndex,
                                                                  @PathVariable Integer pageSize,
                                                                  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        VoTransferReq transferReq = new VoTransferReq();
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferedlist(transferReq);
    }

    @ApiOperation("可转让列表")
    @GetMapping("v2/transferMay/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewTransferMayWarpRes> transferMayList(@PathVariable Integer pageIndex,
                                                                    @PathVariable Integer pageSize,
                                                                    @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoTransferReq transferReq = new VoTransferReq();
        transferReq.setUserId(userId);
        transferReq.setPageIndex(pageIndex);
        transferReq.setPageSize(pageSize);
        return transferBiz.transferMayList(transferReq);
    }


    @ApiOperation("债券转让说明")
    @GetMapping("/v2/transfer/desc")
    public ResponseEntity<String> desc() {
        String content;
        try {
            content = thymeleafHelper.build("tender/translate", null);
        } catch (Throwable e) {
            content = thymeleafHelper.build("load_error", null);
        }
        return ResponseEntity.ok(content);
    }

    /**
     * 债权转让
     *
     * @param voTransferTenderReq
     * @return
     */
    @ApiOperation("债权转让")
    @PostMapping("v2/transfer")
    public ResponseEntity<VoBaseResp> transferTender(@ModelAttribute @Valid VoTransferTenderReq voTransferTenderReq,
                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voTransferTenderReq.setUserId(userId);
        try {
            return transferBiz.newTransferTender(voTransferTenderReq);
        } catch (Exception e) {
            log.error("债权转让异常：", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后重试!"));
        }
    }

    /**
     * 获取立即转让详情
     *
     * @param tenderId 投标记录Id
     * @return
     */
    @ApiOperation("获取立即转让详情")
    @GetMapping("v2/transfer/info/{tenderId}")
    public ResponseEntity<VoGoTenderInfo> goTenderInfo(@PathVariable Long tenderId,
                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return transferBiz.goTenderInfo(tenderId, userId);
    }

    @ApiOperation("购买债券记录")
    @Autowired
    @GetMapping("v2/transfer/user/list/{transferId}")
    private ResponseEntity<VoBorrowTenderUserWarpListRes> tenderList(@PathVariable Long borrowId) {
        return transferBiz.transferUserList(borrowId);
    }


    @Autowired
    @ApiOperation("债券购买次数")
    @GetMapping("v2/transfer/buyCount/{transferId}")
    private ResponseEntity<Integer> buyCount(@PathVariable Long borrowId) {
        return transferBiz.transferBuyCount(borrowId);
    }

}
