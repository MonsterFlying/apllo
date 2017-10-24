package com.gofobao.framework.system.controller;

import com.gofobao.framework.system.biz.ApplicationBiz;
import com.gofobao.framework.system.biz.SysVersionBiz;
import com.gofobao.framework.system.entity.Application;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/version/checkVersion")
    @ApiOperation("版本检查 params: 版本号 versionId:1 ,请求来源  requestSource: 1:Android,2:ios,3:H5")
    public void checkVersion(@RequestHeader("version") Integer versionId,
                             HttpServletResponse response) {
        sysVersionBiz.list(1, versionId, response);
    }


    @GetMapping("version/application/list")
    public List<Application> list(@RequestParam(value = "name",required = false) String name) {
        Application application = new Application();
        application.setAliasName(name);
        return applicationBiz.list(application);
    }



}
