package com.gofobao.framework.message.repository;

import com.gofobao.framework.message.entity.SmsTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Max on 2017/5/17.
 */
@Repository
public interface SmsTemplateRepository extends JpaRepository<SmsTemplateEntity, Long>{

    List<SmsTemplateEntity> findByAliasCode(String alias);
}
