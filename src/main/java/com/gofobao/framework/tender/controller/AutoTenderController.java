package com.gofobao.framework.tender.controller;

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
@Api(description = "自动投标规则控制器")
@RequestMapping
public class AutoTenderController {

    @Autowired
    private AutoTenderBiz autoTenderBiz;

    /**
     * 开启自动投标
     *
     * @param voOpenAutoTenderReq
     * @return
     */
    @ApiOperation("开启自动投标")
    @PostMapping("/autoTender/v2/open")
    public ResponseEntity<VoBaseResp> openAutoTender(@ModelAttribute @Valid VoOpenAutoTenderReq voOpenAutoTenderReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voOpenAutoTenderReq.setUserId(userId);
        return autoTenderBiz.openAutoTender(voOpenAutoTenderReq);
    }

    /**
     * 删除自动投标跪着
     *
     * @param voDelAutoTenderReq
     * @return
     */
    @ApiOperation("删除自动投标")
    @PostMapping("/autoTender/v2/del")
    public ResponseEntity<VoBaseResp> delAutoTender(@ModelAttribute @Valid VoDelAutoTenderReq voDelAutoTenderReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voDelAutoTenderReq.setUserId(userId);
        return autoTenderBiz.delAutoTender(voDelAutoTenderReq);
    }

    /**
     * 创建自动投标规则
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    @ApiOperation("创建自动投标")
    @PostMapping("/autoTender/v2/create")
    public ResponseEntity<VoBaseResp> createAutoTender(@ModelAttribute @Valid VoSaveAutoTenderReq voSaveAutoTenderReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voSaveAutoTenderReq.setUserId(userId);
        return autoTenderBiz.createAutoTender(voSaveAutoTenderReq);
    }

    /**
     * 更新自动投标规则
     *
     * @param voSaveAutoTenderReq
     * @return
     */
    @ApiOperation("更新自动投标规则")
    @PostMapping("/autoTender/v2/update")
    public ResponseEntity<VoBaseResp> updateAutoTender(@ModelAttribute @Valid VoSaveAutoTenderReq voSaveAutoTenderReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voSaveAutoTenderReq.setUserId(userId);
        return autoTenderBiz.updateAutoTender(voSaveAutoTenderReq);
    }

    /**
     * 查询自动投标详情
     *
     * @param autoTenderId
     * @param userId
     * @return
     */
    @ApiOperation("查询自动投标说明")
    @PostMapping("/autoTender/v2/info")
    public ResponseEntity<VoAutoTenderInfo> queryAutoTenderInfo(@RequestParam("autoTenderId") Long autoTenderId,  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY)Long userId) {
        return autoTenderBiz.queryAutoTenderInfo(autoTenderId, userId);
    }


    /**
     * 自动投标说明
     *
     * @return
     * @throws Exception
     */
    @ApiOperation("自动投标说明")
    @GetMapping("/pub/autoTender/v2/desc")
    public ResponseEntity<VoHtmlResp> autoTenderDesc() {
        return autoTenderBiz.autoTenderDesc();
    }

    /**
     * 获取自动投标列表
     *
     * @param voGetAutoTenderList
     * @return
     * @throws Exception
     */
    @ApiOperation("获取自动投标列表")
    @PostMapping("/autoTender/v2/list")
    public ResponseEntity<VoViewAutoTenderList> getAutoTenderList(@ModelAttribute @Valid VoGetAutoTenderList voGetAutoTenderList, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        voGetAutoTenderList.setUserId(userId);
        return autoTenderBiz.getAutoTenderList(voGetAutoTenderList);
    }
}
