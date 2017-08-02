package com.gofobao.framework.tender.repository;

import com.gofobao.framework.tender.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Created by Zeke on 2017/7/31.
 */
@Repository
public interface TransferRepository extends JpaSpecificationExecutor<Transfer>, JpaRepository<Transfer, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Transfer findById(long id);
}
