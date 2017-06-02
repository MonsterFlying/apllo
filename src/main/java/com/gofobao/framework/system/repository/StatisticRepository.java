package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.Statistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Max on 17/6/2.
 */
@Repository
public interface StatisticRepository extends JpaRepository<Statistic, Long> {
}
