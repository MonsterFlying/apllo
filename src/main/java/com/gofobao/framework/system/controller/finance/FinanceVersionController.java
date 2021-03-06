package com.gofobao.framework.system.controller.finance;

import com.gofobao.framework.system.biz.SysVersionBiz;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/6/21.
 */
@RestController
@Api(description = "版本检查")
public class FinanceVersionController {

    @Autowired
    private SysVersionBiz sysVersionBiz;

    @PostMapping("/version/finance/checkVersion")
    @ApiOperation("版本检查 params: 版本号 versionId:1 ,请求来源  requestSource: 1:Android,2:ios,3:H5,4:理财计划")
    public void checkVersion(@RequestHeader("version") Integer versionId,
                             HttpServletResponse response) {
         sysVersionBiz.list(4, versionId,response);
    }
}
