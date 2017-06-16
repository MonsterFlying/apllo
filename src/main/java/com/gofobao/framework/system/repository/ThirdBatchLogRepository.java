package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.ThirdBatchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/6/15.
 */
@Repository
public interface ThirdBatchLogRepository extends JpaRepository<ThirdBatchLog,Long>,JpaSpecificationExecutor<ThirdBatchLog> {

}
