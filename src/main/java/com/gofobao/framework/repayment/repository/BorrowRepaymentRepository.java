package com.gofobao.framework.repayment.repository;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Zeke on 2017/5/26.
 */
@Repository
public interface BorrowRepaymentRepository extends JpaRepository<BorrowRepayment, Long>,JpaSpecificationExecutor<BorrowRepayment> {

    /**
     *
     * @param borrowIds
     * @param status
     * @return
     */
    List<BorrowRepayment> findByBorrowIdInAndStatusIs(List<Long> borrowIds, Integer status);


    /**
     *
     * @param borrowId
     * @return
     */
    List<BorrowRepayment> findByBorrowId(Long borrowId);

}
