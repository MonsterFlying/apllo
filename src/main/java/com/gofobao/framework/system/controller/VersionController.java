package com.gofobao.framework.system.controller;

import com.gofobao.framework.system.biz.ApplicationBiz;
import com.gofobao.framework.system.biz.ApplicationVersionBiz;
import com.gofobao.framework.system.biz.SysVersionBiz;
import com.gofobao.framework.system.entity.Application;
import com.gofobao.framework.system.vo.response.ApplicationWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/6/21.
 */
@RestController
@Api(description = "版本检查")
public class VersionController {

    @Autowired
    private SysVersionBiz sysVersionBiz;

    @Autowired
    private ApplicationBiz applicationBiz;

    @Autowired
    private ApplicationVersionBiz applicationVersionBiz;

    @PostMapping("/version/checkVersion")
    @ApiOperation("版本检查 params: 版本号 versionId:1 ,请求来源  requestSource: 1:Android,2:ios,3:H5")
    public void checkVersion(@RequestHeader("version") Integer versionId,
                             HttpServletResponse response) {
        sysVersionBiz.list(1, versionId, response);
    }


    @GetMapping("application/version/list")
    @ApiOperation("应用别名 aliasName:理财计划:finance,金服：financial_service。terminal 终端 1:android, 2：ios, 3:h5  ")
    public ResponseEntity<ApplicationWarpRes> list(@RequestHeader(value = "aliasName") String aliasName,
                                                   @RequestHeader(value = "terminal", required = false) Integer terminal) throws Exception {
        Application application = new Application();
        if (!StringUtils.isEmpty(terminal)) {
            application.setTerminal(terminal);
        }
        application.setAliasName(aliasName);
        return applicationBiz.list(application);
    }

    @ApiOperation("版本检查 params: 版本号 versionId:1 ,应用编号 applicationId：１")
    @PostMapping("application/version/checkVersion")
    public void recheckVersion(HttpServletResponse response,
                               @RequestHeader("applicationId") Integer applicationId,
                               @RequestHeader("versionId") Integer versionId) {
        applicationVersionBiz.recheckVersion(applicationId, versionId, response);
    }

}
