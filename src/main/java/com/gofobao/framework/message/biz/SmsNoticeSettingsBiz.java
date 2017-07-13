package com.gofobao.framework.message.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.entity.SmsNoticeSettingsEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by admin on 2017/7/13.
 */
public interface SmsNoticeSettingsBiz {

    ResponseEntity<List<SmsNoticeSettingsEntity>> list(Integer userId);

    ResponseEntity<VoBaseResp> update(SmsNoticeSettingsEntity smsNoticeSettingsEntity);

}
