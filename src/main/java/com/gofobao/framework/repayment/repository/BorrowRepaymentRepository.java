package com.gofobao.framework.repayment.repository;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<BorrowRepayment> findByBorrowIdInAndStatusIs(List<Long> borrowIds,Integer status);


    /**
     * 还款中标列表
     * userId=? and status=? and successAt!=null and closeAt ==null
     * @param userId
     * @param status
     * @return
     */
     Page<Borrow> findByUserIdEqAndStatusEqAndSuccessAtIsNONullAndCloseAtIsNull(Long userId, int status, Pageable pageable);


    /**
     * 已结清标列表
     * userId=? and status=? and successAt!=null and closeAt !=null
     * @param userId
     * @param status
     * @return
     *
     */
    Page<Borrow>findByUserIdEqAndStatusEqAndSuccessAtIsNONullAndCloseAtIsNotNull(Long userId,int status,Pageable pageable);


}
