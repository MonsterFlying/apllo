package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.IncrStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Max on 17/6/2.
 */
@Repository
public interface IncrStatisticRepository  extends JpaRepository<IncrStatistic, Long> ,JpaSpecificationExecutor<IncrStatistic>{
}
