package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.AdvanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Created by Zeke on 2017/6/7.
 */
@Repository
public interface AdvanceLogRepository extends JpaRepository<AdvanceLog,Long>,JpaSpecificationExecutor<AdvanceLog>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    AdvanceLog findById(Long id);

    AdvanceLog findByRepaymentId(Long repaymentId);
}
