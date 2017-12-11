package com.gofobao.framework.contract.repository;

import com.gofobao.framework.contract.entity.BorrowContract;
import com.gofobao.framework.finance.entity.FinancePlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface BorrowContractRepository extends JpaRepository<BorrowContract, Long>, JpaSpecificationExecutor<BorrowContract> {

    List<BorrowContract> findByBorrowIdAndBatchNoAndStatus(Long borrowId, String batchNo, Boolean status);

    @Modifying
    @Transactional
    @Query(" UPDATE  BorrowContract borrowContract SET borrowContract.status=true  WHERE borrowContract.borrowId=?1 AND borrowContract.batchNo=?2")
    void updateContractStatus(Long borrowId, String batchNo);

    @Query("SELECT borrowContract FROM BorrowContract borrowContract " +
            "WHERE " +
            "(borrowContract.userId =?1 OR borrowContract.forUserId=?1) " +
            "AND " +
            "borrowContract.status=1 " +
            "AND " +
            "borrowContract.type=?2 " )
    List<BorrowContract> findUserContracts(Long userId, Integer type, Pageable pageable);
}
