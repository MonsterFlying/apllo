package com.gofobao.framework.message.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.SmsNoticeSettingsBiz;
import com.gofobao.framework.message.entity.SmsNoticeSettingsEntity;
import com.gofobao.framework.message.service.SmsNoticeSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Created by admin on 2017/7/13.
 */
@Service
public class SmsNoticeSettingBizImpl implements SmsNoticeSettingsBiz {

    @Autowired
    private SmsNoticeSettingsService smsNoticeSettingsService;

    @Override
    public ResponseEntity<List<SmsNoticeSettingsEntity>> list(Integer userId) {

        List<SmsNoticeSettingsEntity> settingsEntityList = smsNoticeSettingsService.findByUserId(userId);

        return ResponseEntity.ok(settingsEntityList);
    }


    @Override
    public ResponseEntity<VoBaseResp> update(SmsNoticeSettingsEntity smsNoticeSettingsEntity) {
        SmsNoticeSettingsEntity settingsEntity = null;
        try {
            settingsEntity = smsNoticeSettingsService.update(smsNoticeSettingsEntity);
            if (ObjectUtils.isEmpty(settingsEntity))

                return ResponseEntity.badRequest()
                        .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候再试！"));
            else
                return ResponseEntity.ok(VoBaseResp.ok("更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候再试！"));
        }
    }
}
