package com.gofobao.framework.lend.controller.web;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.lend.biz.LendBiz;
import com.gofobao.framework.lend.vo.request.*;
import com.gofobao.framework.lend.vo.response.*;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by admin on 2017/6/6.
 */
@Api(description = "pc：出借")
@RestController
@RequestMapping("")
public class WebLendController {

    @Autowired
    private LendBiz lendBiz;

    private VoUserLendReq voUserLendReq = new VoUserLendReq();

    @RequestMapping(value = "lend/pc/v2/list/{pageIndex}/{pageSize}", method = RequestMethod.GET)
    @ApiOperation("pc：出借列表")
    public ResponseEntity<VoViewLendListWarpRes> list(@PathVariable Integer pageIndex,
                                                      @PathVariable Integer pageSize,
                                                      @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        Page page = new Page();
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return lendBiz.list(page,userId);
    }


    @RequestMapping(value = "lend/pc/v2/info/{lendId}", method = RequestMethod.GET)
    @ApiOperation("pc：出借详情")
    public ResponseEntity<VoViewLendInfoWarpRes> info(@PathVariable Long lendId,
                                                      @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return lendBiz.info(userId, lendId);
    }

    @RequestMapping(value = "lend/pc/v2/info/list/{lendId}", method = RequestMethod.GET)
    @ApiOperation("pc：出借详情列表")
    public ResponseEntity<VoViewLendInfoListWarpRes> infoList(@PathVariable Long lendId ,
                                                      @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return lendBiz.infoList(userId, lendId);
    }

    @RequestMapping(value = "lend/pc/v2/list/byUser/{pageIndex}/{pageSize}", method = RequestMethod.GET)
    @ApiOperation("pc：我的出借列表")
    public ResponseEntity<VoViewUserLendInfoWarpRes> byUser(
            @PathVariable Integer pageIndex,
            @PathVariable Integer pageSize,
            @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voUserLendReq.setPageSize(pageSize);
        voUserLendReq.setPageIndex(pageIndex);
        voUserLendReq.setUserId(userId);
        return lendBiz.byUserId(voUserLendReq);
    }

    /**
     * 发布有草出借
     *
     * @param voCreateLend
     * @return
     */
    @PostMapping(value = "lend/pc/v2/create")
    @ApiOperation("pc:发布有草出借")
    public ResponseEntity<VoBaseResp> pcCreate(@ModelAttribute @Valid VoCreateLend voCreateLend,
                                             @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voCreateLend.setUserId(userId);
        return lendBiz.create(voCreateLend);
    }

    /**
     * 获取当前用户黑名单列表
     *
     * @param voGetLendBlacklists
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "获取当前用户黑名单列表", notes = "获取当前用户黑名单列表")
    @PostMapping(value = "lend/pc/userLendBlacklist")
    public ResponseEntity<VoViewLendBlacklists> getLendBlacklists(@ModelAttribute @Valid VoGetLendBlacklists voGetLendBlacklists,
                                                                  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voGetLendBlacklists.setUserId(userId);
        return lendBiz.getLendBlacklists(voGetLendBlacklists);
    }

    /**
     * 添加有草出借黑名单
     *
     * @param voAddLendBlacklist
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "添加有草出借黑名单", notes = "添加有草出借黑名单")
    @PostMapping(value = "lend/pc/v2/blacklist/add")
    public ResponseEntity<VoBaseResp> addLendBlacklist(@ModelAttribute @Valid VoAddLendBlacklist voAddLendBlacklist,
                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAddLendBlacklist.setUserId(userId);
        return lendBiz.addLendBlacklist(voAddLendBlacklist);
    }

    /**
     * 移除有草出借黑名单
     *
     * @param voDelLendBlacklist
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "移除有草出借黑名单", notes = "移除有草出借黑名单")
    @PostMapping(value = "lend/pc/v2/blacklist/remove")
    public ResponseEntity<VoBaseResp> delLendBlacklist(@ModelAttribute @Valid VoDelLendBlacklist voDelLendBlacklist,
                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voDelLendBlacklist.setUserId(userId);
        return lendBiz.delLendBlacklist(voDelLendBlacklist);
    }
}
