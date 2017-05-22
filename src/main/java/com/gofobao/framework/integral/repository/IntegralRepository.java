package com.gofobao.framework.integral.repository;

import com.gofobao.framework.integral.entity.Integral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface IntegralRepository extends JpaRepository<Integral,Long>{
}
