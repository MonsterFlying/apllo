package com.gofobao.framework.currency.repository;

import com.gofobao.framework.currency.entity.CurrencyLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Zeke on 2017/5/23.
 */
@Repository
public interface CurrencyLogRepository extends JpaRepository<CurrencyLog,Long>,JpaSpecificationExecutor<CurrencyLog> {
    List<CurrencyLog> findByUserId(Long userId, Pageable pageable);
}
