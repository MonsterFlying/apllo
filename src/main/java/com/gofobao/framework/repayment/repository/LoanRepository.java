package com.gofobao.framework.repayment.repository;

import com.gofobao.framework.tender.entity.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by admin on 2017/6/2.
 */

/**
 * 我的借款
 */
public interface LoanRepository extends JpaRepository<Tender,Long>,JpaSpecificationExecutor<Tender> {
}
