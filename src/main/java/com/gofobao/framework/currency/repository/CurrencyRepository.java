package com.gofobao.framework.currency.repository;

import com.gofobao.framework.currency.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Created by Zeke on 2017/5/23.
 */
@Repository
public interface CurrencyRepository extends JpaRepository<Currency,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Currency findByUserId(Long userId);
}
