package com.gofobao.framework.borrow.repository;

import com.gofobao.framework.borrow.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Max on 17/5/16.
 */
@Repository
public interface BorrowRepository extends JpaRepository<Borrow,Long> {

}
