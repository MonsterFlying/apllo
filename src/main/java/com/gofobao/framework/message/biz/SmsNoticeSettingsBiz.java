package com.gofobao.framework.message.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.entity.SmsNoticeSettingsEntity;
import com.gofobao.framework.system.vo.response.SmsNoticeListRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/7/13.
 */
public interface SmsNoticeSettingsBiz {

    ResponseEntity<SmsNoticeListRes> list(Long userId);

    ResponseEntity<VoBaseResp> update(SmsNoticeSettingsEntity smsNoticeSettingsEntity);

}
