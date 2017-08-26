package com.gofobao.framework.member.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.SmsNoticeSettingsBiz;
import com.gofobao.framework.message.entity.SmsNoticeSettingsEntity;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by admin on 2017/7/13.
 */
@RestController
@RequestMapping("")
@Api(description="pc：短信设置")
public class WebSmsNoticeSettingController {

    @Autowired
    private SmsNoticeSettingsBiz smsNoticeSettingsBiz;

    @ApiOperation("短信服务设置")
    @PostMapping("smsSetting/pc/v2/update")
    public ResponseEntity<VoBaseResp> update(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                            SmsNoticeSettingsEntity smsSettingUpdateReq) {
        smsSettingUpdateReq.setUserId(userId);
    return smsNoticeSettingsBiz.update(smsSettingUpdateReq);
    }


    /**
     * 用户短信设置列表
     *
     * @return
     */
    @ApiOperation("用户短信设置列表")
    @PostMapping("smmSetting/pc/v2/list")
    public ResponseEntity<List<SmsNoticeSettingsEntity>> list(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return smsNoticeSettingsBiz.list(userId);
    }
}
