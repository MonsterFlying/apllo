package com.gofobao.framework.integral.repository;

import com.gofobao.framework.integral.entity.Integral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface IntegralRepository extends JpaRepository<Integral,Long>{
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    Integral findByUserId(Long userId);
}
