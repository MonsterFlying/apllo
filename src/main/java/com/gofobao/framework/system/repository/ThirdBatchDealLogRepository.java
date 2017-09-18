package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.ThirdBatchDealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/6/15.
 */
@Repository
public interface ThirdBatchDealLogRepository extends JpaRepository<ThirdBatchDealLog,Long>,JpaSpecificationExecutor<ThirdBatchDealLog> {
}
