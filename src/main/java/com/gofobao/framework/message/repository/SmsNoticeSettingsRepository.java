package com.gofobao.framework.message.repository;

import com.gofobao.framework.message.entity.SmsNoticeSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/7/13.
 */
@Repository
public interface SmsNoticeSettingsRepository extends JpaRepository<SmsNoticeSettingsEntity,Long>, JpaSpecificationExecutor<SmsNoticeSettingsEntity>{

        List<SmsNoticeSettingsEntity>findByUserId(Long userId);

}
