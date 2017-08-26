package com.gofobao.framework.message.service.impl;

import com.gofobao.framework.message.entity.SmsNoticeSettingsEntity;
import com.gofobao.framework.message.repository.SmsNoticeSettingsRepository;
import com.gofobao.framework.message.service.SmsNoticeSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by admin on 2017/7/13.
 */
@Component
public class SmsNoticeSettingsServiceImpl implements SmsNoticeSettingsService{

    @Autowired
    private SmsNoticeSettingsRepository smsNoticeSettingsRepository;

    /**
     * 列表
     * @param userId
     * @return
     */
    @Override
    public List<SmsNoticeSettingsEntity> findByUserId(Long userId) {
      return   smsNoticeSettingsRepository.findByUserId(userId);
    }

    /**
     * 更新
     * @param smsNoticeSettingsEntity
     * @return
     */
    @Override
    public SmsNoticeSettingsEntity update(SmsNoticeSettingsEntity smsNoticeSettingsEntity) {
        return  smsNoticeSettingsRepository.save(smsNoticeSettingsEntity);

    }

    /**
     *
     * @param smsNoticeSettingsEntity
     * @return
     */
    @Override
    public SmsNoticeSettingsEntity save(SmsNoticeSettingsEntity smsNoticeSettingsEntity) {
        return smsNoticeSettingsRepository.save(smsNoticeSettingsEntity);
    }
}
