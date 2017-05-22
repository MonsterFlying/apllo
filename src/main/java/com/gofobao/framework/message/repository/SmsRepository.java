package com.gofobao.framework.message.repository;

import com.gofobao.framework.message.entity.SmsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Max on 17/5/18.
 */
@Repository
public interface SmsRepository extends JpaRepository<SmsEntity, Long>{
}
