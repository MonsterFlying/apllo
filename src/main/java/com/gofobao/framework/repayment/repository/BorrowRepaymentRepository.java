package com.gofobao.framework.repayment.repository;

import com.gofobao.framework.repayment.entity.BorrowRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/5/26.
 */
@Repository
public interface BorrowRepaymentRepository extends JpaRepository<BorrowRepayment, Long>,JpaSpecificationExecutor<BorrowRepayment> {
}
