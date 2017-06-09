package com.gofobao.framework.award.repository;

import com.gofobao.framework.borrow.entity.BorrowVirtual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by admin on 2017/6/9.
 */
@Repository
public interface VirtualBorrowRepository extends JpaRepository<BorrowVirtual,Long>,JpaSpecificationExecutor<BorrowVirtual> {

    List<BorrowVirtual> findByStatus(Integer status);

}
