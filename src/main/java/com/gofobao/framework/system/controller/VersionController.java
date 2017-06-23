package com.gofobao.framework.system.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.SysVersionBiz;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/21.
 */
@RestController
@Api("版本检查")
public class VersionController {

    @Autowired
    private SysVersionBiz sysVersionBiz;

    @ApiOperation("版本检查 params: 版本号 versionId:1 ,请求来源  requestSource: 1:Android,2:ios,3:H5")
    @PostMapping("pub/v2/version/check")
    public ResponseEntity<VoBaseResp> checkVersion(@RequestHeader("requestSource") Integer terminal,
                                                   @RequestHeader("versionId") Integer versionId) {
        return sysVersionBiz.list(terminal, versionId);
    }


}
