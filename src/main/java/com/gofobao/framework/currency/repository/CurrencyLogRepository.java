package com.gofobao.framework.currency.repository;

import com.gofobao.framework.currency.entity.CurrencyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/5/23.
 */
@Repository
public interface CurrencyLogRepository extends JpaRepository<CurrencyLog,Long> {

}
