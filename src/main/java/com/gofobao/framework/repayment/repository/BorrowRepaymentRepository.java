package com.gofobao.framework.repayment.repository;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    BorrowRepayment findById(Long id);


    @Query("SELECT borrowRepayment FROM  BorrowRepayment borrowRepayment\n" +
            "where\n" +
            "borrowRepayment.userId=?1\n" +
            "AND\n" +
            "(DATE_FORMAT(borrowRepayment.repayAt,'%Y%m%d')=current_date\n" +
            "OR\n" +
            "(DATE_FORMAT(borrowRepayment.repayAtYes,'%Y%m%d')=current_date))")
    List<BorrowRepayment>todayRepayment(Long userId);

}
