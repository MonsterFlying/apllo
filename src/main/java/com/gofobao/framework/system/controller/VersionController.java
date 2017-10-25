package com.gofobao.framework.system.controller;

import com.gofobao.framework.helper.jsonfilter.JSON;
import com.gofobao.framework.system.biz.ApplicationBiz;
import com.gofobao.framework.system.biz.ApplicationVersionBiz;
import com.gofobao.framework.system.biz.SysVersionBiz;
import com.gofobao.framework.system.entity.Application;
import com.gofobao.framework.system.entity.ApplicationVersion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    @ApiOperation("应用别名 aliasName:理财计划:finance,金服：financialService。terminal 终端 1:android, 2：ios, 3:h5  ")
    @JSON(type = Application.class,filter = "id")
    public List<Application> list(@RequestParam(value = "aliasName", required = false) String aliasName,
                                  @RequestParam(value = "terminal") Integer terminal) throws Exception {
        Application application = new Application();
        application.setTerminal(terminal);
        application.setAliasName(aliasName);
        return applicationBiz.list(application);
    }

    @ApiOperation("版本检查 params: 版本号 versionId:1 ,请求来源  applicationId")
    @PostMapping("application/version/checkVersion")
    public void recheckVersion(HttpServletResponse response,
                               @RequestHeader("applicationId") Integer applicationId,
                               @RequestHeader("versionId") Integer versionId) {
        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setApplicationId(applicationId);
        applicationVersion.setVersionId(versionId);
        applicationVersionBiz.recheckVersion(applicationVersion, response);
    }


}
