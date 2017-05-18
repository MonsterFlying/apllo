package com.gofobao.framework.message.repository;

import com.gofobao.framework.message.entity.GfbSmsTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Max on 2017/5/17.
 */
public interface SmsTemplateRepository extends JpaRepository<GfbSmsTemplateEntity, Long>{

    List<GfbSmsTemplateEntity> findByAliasCode(String alias);
}
