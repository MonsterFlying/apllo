package com.gofobao.framework.lend.controller.web;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.lend.biz.LendBiz;
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

/**
 * Created by admin on 2017/6/6.
 */
@Api(description = "出借")
@RestController
@RequestMapping("/lend/pc")
public class WebLendController {

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
    @ApiOperation("出借想起")
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


}
