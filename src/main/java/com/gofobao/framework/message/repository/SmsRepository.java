package com.gofobao.framework.message.repository;

import com.gofobao.framework.message.entity.SmsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Max on 17/5/18.
 */
public interface SmsRepository extends JpaRepository<SmsEntity, Long>{
}
