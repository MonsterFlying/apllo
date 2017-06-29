package com.gofobao.framework.award.controller;

import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.award.vo.request.VoOpenRedPackageReq;
import com.gofobao.framework.award.vo.request.VoRedPackageReq;
import com.gofobao.framework.award.vo.response.VoViewOpenRedPackageWarpRes;
import com.gofobao.framework.award.vo.response.VoViewRedPackageWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/6/7.
 */
@RestController
@Api(description="红包")
public class RedPackageController {

    @Autowired
    private RedPackageBiz redPackageBiz;

    /**
     * 红包列表
     *
     * @param status
     * @param pageIndex
     * @param pageSize
     * @param userId
     * @return
     */
    @ApiOperation("status 0：未领取；1：已领取；2：已过期")
    @GetMapping("redPackage/v2/list/{status}/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewRedPackageWarpRes> list(@PathVariable Integer status,
                                                        @PathVariable Integer pageIndex,
                                                        @PathVariable Integer pageSize,
                                                        @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoRedPackageReq voRedPackageReq = new VoRedPackageReq();
        voRedPackageReq.setPageSize(pageSize);
        voRedPackageReq.setUserId(userId);
        voRedPackageReq.setPageIndex(pageIndex);
        voRedPackageReq.setStatus(status);
        return redPackageBiz.list(voRedPackageReq);

    }

    /**
     * 拆开红包
     *
     * @param voOpenRedPackageReq
     * @return
     */
    @PostMapping("redPackage/v2/open")
    public ResponseEntity<VoViewOpenRedPackageWarpRes> openRedPackage(@ModelAttribute VoOpenRedPackageReq voOpenRedPackageReq,
                                                                      @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voOpenRedPackageReq.setUserId(userId);
        return redPackageBiz.openRedPackage(voOpenRedPackageReq);
    }
}
