package com.gofobao.framework.repayment.repository;

import com.gofobao.framework.borrow.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by admin on 2017/6/2.
 */

/**
 * 我的借款
 */
public interface LoanRepository extends JpaRepository<Borrow,Long>,JpaSpecificationExecutor<Borrow> {


    /**
     * 还款中标列表
     * userId=? and status=? and successAt!=null and closeAt ==null and tender==null
     * @param userId
     * @param status
     * @return
     */
    Page<Borrow> findByUserIdAndStatusIsAndSuccessAtIsNotNullAndCloseAtIsNullAndTenderIdIsNull(Long userId, int status, Pageable pageable);


    /**
     * 已结清标列表
     * userId=? and status=? and successAt!=null and closeAt !=null and tender==null
     * @param userId
     * @param status
     * @return
     *
     */
    Page<Borrow> findByUserIdAndStatusIsAndSuccessAtIsNotNullAndCloseAtIsNotNullAndTenderIdIsNull(Long userId, int status, Pageable pageable);



    /**
     * 招标中
     * user=? and status=? verifyAt!=null
     * @param userId
     * @param status
     * @param pageable
     * @return
     */
    Page<Borrow> findByUserIdAndStatusIsAndVerifyAtIsNotNull(Long userId, int status, Pageable pageable);


}
