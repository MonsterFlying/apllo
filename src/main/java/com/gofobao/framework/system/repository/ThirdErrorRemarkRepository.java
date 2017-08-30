package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.ThirdErrorRemark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Created by Zeke on 2017/8/25.
 */
@Repository
public interface ThirdErrorRemarkRepository extends JpaRepository<ThirdErrorRemark, Long>, JpaSpecificationExecutor<ThirdErrorRemark> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ThirdErrorRemark findById(long remarkId);
}
