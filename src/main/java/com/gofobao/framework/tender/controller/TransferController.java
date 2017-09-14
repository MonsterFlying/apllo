package com.gofobao.framework.tender.controller;

import cn.jiguang.common.utils.StringUtils;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.tender.biz.TransferBiz;
import com.gofobao.framework.tender.vo.request.*;
import com.gofobao.framework.tender.vo.response.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

/**
 * Created by admin on 2017/6/12.
 */
@RestController
@RequestMapping
@Api(description = "债权相关控制器")
@Slf4j
@SuppressWarnings("all")
public class TransferController {

    @Autowired
    private TransferBiz transferBiz;

    @Autowired
    private ThymeleafHelper thymeleafHelper;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    /**
     * 查询债权转让购买记录
     *
     * @return
     */
    @ApiOperation("查询债权转让购买记录")
    @PostMapping("transfer/v2/buy/list")
    public ResponseEntity<VoViewTransferBuyLogList> findTransferBuyLog(@Valid @ModelAttribute VoFindTransferBuyLog voFindTransferBuyLog, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voFindTransferBuyLog.setUserId(userId);
        return transferBiz.findTransferBuyLog(voFindTransferBuyLog);
    }

    /**
     * 新版结束债权
     *
     * @param voEndTransfer
     * @return
     * @throws Exception
     */
    @ApiOperation("结束债权转让")
    @PostMapping("transfer/v2/new/transfer/end")
    public ResponseEntity<VoBaseResp> endTransfer(@Valid @ModelAttribute VoEndTransfer voEndTransfer, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        try {
            voEndTransfer.setUserId(userId);
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
    @PostMapping("transfer/v2/new/transfer/publish")
    public ResponseEntity<VoBaseResp> newTransferTender(@Valid @ModelAttribute VoTransferTenderReq voTransferTenderReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        try {
            voTransferTenderReq.setUserId(userId);
            return transferBiz.newTransferTender(voTransferTenderReq);
        } catch (Exception e) {
            log.error("债权转让异常:", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后重试!"));
        }
    }

    /**
     * 购买债权转让
     */
    @ApiOperation("购买债权转让")
    @PostMapping("transfer/v2/new/transfer/buy")
    public ResponseEntity<VoBaseResp> buyTransfer(@Valid @ModelAttribute VoBuyTransfer voBuyTransfer, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        try {
            voBuyTransfer.setUserId(userId);
            return transferBiz.buyTransfer(voBuyTransfer);
        } catch (Exception e) {
            log.error("购买债权转让异常：", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后重试!"));
        }
    }

    @ApiOperation("转让中列表")
    @GetMapping("transfer/v2/transferOf/list/{pageIndex}/{pageSize}")
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
    @GetMapping("transfer/v2/transfered/list/{pageIndex}/{pageSize}")
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
    @GetMapping("transfer/v2/transferMay/list/{pageIndex}/{pageSize}")
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
    @GetMapping("/pub/transfer/v2/transfer/desc")
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
     * 获取立即转让详情
     *
     * @param tenderId 投标记录Id
     * @return
     */
    @ApiOperation("获取立即转让详情")
    @GetMapping("transfer/v2/transfer/info/{tenderId}")
    public ResponseEntity<VoGoTenderInfo> goTenderInfo(@PathVariable Long tenderId,
                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return transferBiz.goTenderInfo(tenderId, userId);
    }

    @ApiOperation("购买债权记录")
    @GetMapping("/pub/transfer/v2/transfer/user/list/{pageIndex}/{pageSize}/{transferId}")
    public ResponseEntity<VoBorrowTenderUserWarpListRes> transferUserList(@PathVariable Long transferId,
                                                                          @PathVariable Integer pageIndex,
                                                                          @PathVariable Integer pageSize,
                                                                          HttpServletRequest request,
                                                                          HttpServletResponse response) {
        VoTransferUserListReq transferUserListReq = new VoTransferUserListReq();
        try {
            String token = jwtTokenHelper.getToken(request);
            if (!StringUtils.isEmpty(token)) {
                jwtTokenHelper.validateSign(token);
                Long userId = jwtTokenHelper.getUserIdFromToken(token);  // 用户ID
                transferUserListReq.setUserId(userId);
            }
        } catch (Exception e) {
            log.info("当前用户未登录");
        }
        transferUserListReq.setPageSize(pageSize);
        transferUserListReq.setPageIndex(pageIndex);
        transferUserListReq.setTransferId(transferId);
        return transferBiz.transferUserList(transferUserListReq);
    }


    @ApiOperation(value = "债权合同")
    @GetMapping(value = "transfer/v2/transferProtocol/{tenderId}")
    public ResponseEntity<String> takeRatesDesc(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable Long tenderId, HttpServletRequest request) throws Exception {
        String content = "";
        Map<String, Object> paramMaps = transferBiz.contract(tenderId, userId);
        try {
            content = thymeleafHelper.build("transferProtocol", paramMaps);
        } catch (Exception e) {
            e.printStackTrace();
            content = thymeleafHelper.build("load_error", null);
        }
        return ResponseEntity.ok(content);
    }


}
