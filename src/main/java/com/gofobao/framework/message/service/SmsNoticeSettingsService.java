package com.gofobao.framework.message.service;

import com.gofobao.framework.message.entity.SmsNoticeSettingsEntity;

import java.util.List;

/**
 * Created by admin on 2017/7/13.
 */
public interface SmsNoticeSettingsService {

        /**
         *
         * @param userId
         * @return
         */
        List<SmsNoticeSettingsEntity> findByUserId(Long userId);

        /**
         *
         * @param smsNoticeSettingsEntity
         * @return
         */
        SmsNoticeSettingsEntity update(SmsNoticeSettingsEntity smsNoticeSettingsEntity);

        /**
         * 
         * @param smsNoticeSettingsEntity
         * @return
         */
        SmsNoticeSettingsEntity save(SmsNoticeSettingsEntity smsNoticeSettingsEntity);
}
