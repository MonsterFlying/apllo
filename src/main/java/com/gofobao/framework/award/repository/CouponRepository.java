package com.gofobao.framework.award.repository;

import com.gofobao.framework.award.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by admin on 2017/6/7.
 */
@Repository
public interface CouponRepository  extends JpaRepository<Coupon,Long>,JpaSpecificationExecutor<Coupon> {
}
