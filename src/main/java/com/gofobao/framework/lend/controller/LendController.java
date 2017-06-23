package com.gofobao.framework.lend.controller;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.lend.biz.LendBiz;
import com.gofobao.framework.lend.vo.request.VoCreateLend;
import com.gofobao.framework.lend.vo.request.VoEndLend;
import com.gofobao.framework.lend.vo.request.VoLend;
import com.gofobao.framework.lend.vo.request.VoUserLendReq;
import com.gofobao.framework.lend.vo.response.VoViewLendInfoWarpRes;
import com.gofobao.framework.lend.vo.response.VoViewLendListWarpRes;
import com.gofobao.framework.lend.vo.response.VoViewUserLendInfoWarpRes;
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
@Api(description = "出借")
@RestController
@RequestMapping("/lend")
public class LendController {

    @Autowired
    private LendBiz lendBiz;

    @RequestMapping(value = "/v2/list/{pageIndex}/{pageSize}", method = RequestMethod.GET)
    @ApiOperation("出借列表")
    public ResponseEntity<VoViewLendListWarpRes> list(@PathVariable Integer pageIndex,
                                                      @PathVariable Integer pageSize) {
        Page page = new Page();
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return lendBiz.list(page);
    }


    @RequestMapping(value = "/v2/info/{lendId}", method = RequestMethod.GET)
    @ApiOperation("出借详情")
    public ResponseEntity<VoViewLendInfoWarpRes> info(@PathVariable Long lendId,
                                                      @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        return lendBiz.info(userId, lendId);
    }

    @RequestMapping(value = "/v2/list/byUser/{pageIndex}/{pageSize}", method = RequestMethod.GET)
    @ApiOperation("我的出借列表")
    public ResponseEntity<VoViewUserLendInfoWarpRes> byUser(
            @PathVariable Integer pageIndex,
            @PathVariable Integer pageSize,
            @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoUserLendReq voUserLendReq = new VoUserLendReq();
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
    @PostMapping(value = "/v2/create")
    @ApiOperation("发布有草出借")
    public ResponseEntity<VoBaseResp> create(@ModelAttribute @Valid VoCreateLend voCreateLend, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voCreateLend.setUserId(userId);
        return lendBiz.create(voCreateLend);

    }

    /**
     * 结束有草出借
     *
     * @param voEndLend
     * @return
     */
    @PostMapping(value = "/v2/end")
    @ApiOperation("结束有草出借")
    public ResponseEntity<VoBaseResp> end(@ModelAttribute @Valid VoEndLend voEndLend, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voEndLend.setUserId(userId);
        return lendBiz.end(voEndLend);
    }

    /**
     * 有草出借摘草
     *
     * @param voLend
     * @return
     */
    @PostMapping(value = "/v2/lend")
    @ApiOperation("有草出借摘草")
    public ResponseEntity<VoBaseResp> lend(@ModelAttribute @Valid VoLend voLend, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voLend.setUserId(userId);
        return lendBiz.lend(voLend);
    }

}
