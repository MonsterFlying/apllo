package com.gofobao.framework.lend.controller;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.lend.biz.LendBiz;
import com.gofobao.framework.lend.vo.response.VoViewLendListWarpRes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/6.
 */
@ApiModel("出借列表")
@RestController
@RequestMapping("/lend")
public class LendController {

    @Autowired
    private LendBiz lendBiz;

    @RequestMapping(value = "/v2/list/{pageIndex}/{pageSize}",method = RequestMethod.GET )
    @ApiOperation("出借列表")
    public ResponseEntity<VoViewLendListWarpRes> list(@PathVariable Integer pageIndex,
                                                      @PathVariable Integer pageSize) {
        Page page = new Page();
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return lendBiz.list(page);
    }


}
