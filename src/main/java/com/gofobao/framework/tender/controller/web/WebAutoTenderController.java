package com.gofobao.framework.tender.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.AutoTenderBiz;
import com.gofobao.framework.tender.vo.request.VoDelAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoGetAutoTenderList;
import com.gofobao.framework.tender.vo.request.VoOpenAutoTenderReq;
import com.gofobao.framework.tender.vo.request.VoSaveAutoTenderReq;
import com.gofobao.framework.tender.vo.response.VoAutoTenderInfo;
import com.gofobao.framework.tender.vo.response.VoViewAutoTenderList;
import com.gofobao.framework.tender.vo.response.web.VoViewPcAutoTenderWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/6/21.
 */
@RestController
@Api(description = "pc:自动投标规则控制器")
@RequestMapping
public class WebAutoTenderController {

    @Autowired
    private AutoTenderBiz autoTenderBiz;

    /**
     * pc:获取自动投标列表
     * @param
     * @return
     * @throws Exception
     */
    @ApiOperation("pc：获取自动投标列表")
    @PostMapping("/autoTender/pc/v2/list")
    public ResponseEntity<VoViewPcAutoTenderWarpRes> autoTenderList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        VoGetAutoTenderList autoTender = new VoGetAutoTenderList();
        autoTender.setUserId(901L);
        return autoTenderBiz.pcAutoTenderList(autoTender);
    }


    /**
     * pc: 开启/关闭 自动投标
     * @param
     * @return
     * @throws Exception
     */
    @ApiOperation("pc:开启/关闭 自动投标")
    @PostMapping("/autoTender/pc/v2/open")
    public ResponseEntity<VoBaseResp> autoTenderList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                     VoOpenAutoTenderReq voOpenAutoTenderReq) throws Exception {
        voOpenAutoTenderReq.setUserId(userId);
        return autoTenderBiz.openAutoTender(voOpenAutoTenderReq);
    }

    /**
     * pc:删除自动投标跪着
     * @param voDelAutoTenderReq
     * @return
     */
    @ApiOperation("pc: 删除自动投标")
    @PostMapping("/autoTender/pc/v2/del")
    public ResponseEntity<VoBaseResp> delAutoTender(@ModelAttribute @Valid VoDelAutoTenderReq voDelAutoTenderReq,
                                                    @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voDelAutoTenderReq.setUserId(userId);
        return autoTenderBiz.delAutoTender(voDelAutoTenderReq);
    }


    /**
     * pc:更新自动投标规则
     * @param voSaveAutoTenderReq
     * @return
     */
    @ApiOperation("pc：更新自动投标规则")
    @PostMapping("/autoTender/pc/v2/update")
    public ResponseEntity<VoBaseResp> updateAutoTender(@ModelAttribute @Valid VoSaveAutoTenderReq voSaveAutoTenderReq,
                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voSaveAutoTenderReq.setUserId(userId);
        return autoTenderBiz.updateAutoTender(voSaveAutoTenderReq);
    }


    /**
     * pc:创建自动投标规则
     * @param voSaveAutoTenderReq
     * @return
     */
    @ApiOperation("pc:创建自动投标")
    @PostMapping("/autoTender/pc/v2/create")
    public ResponseEntity<VoBaseResp> createAutoTender(@ModelAttribute @Valid VoSaveAutoTenderReq voSaveAutoTenderReq,
                                                       @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voSaveAutoTenderReq.setUserId(userId);
        return autoTenderBiz.createAutoTender(voSaveAutoTenderReq);
    }
}
