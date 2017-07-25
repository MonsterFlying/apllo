package com.gofobao.framework.integral.repository;

import com.gofobao.framework.integral.entity.IntegralLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface IntegralLogRepository extends JpaRepository<IntegralLog,Long>,JpaSpecificationExecutor<IntegralLog>{
    List<IntegralLog> findByUserId(Long userId, Pageable pageable);


}
