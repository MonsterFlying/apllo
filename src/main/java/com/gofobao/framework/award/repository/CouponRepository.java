package com.gofobao.framework.award.repository;

import com.gofobao.framework.award.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by admin on 2017/6/7.
 */
@Repository
public interface CouponRepository  extends JpaRepository<Coupon,Long>,JpaSpecificationExecutor<Coupon> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Coupon> findById(Long id);
}
