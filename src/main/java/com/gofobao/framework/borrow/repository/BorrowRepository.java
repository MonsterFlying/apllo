package com.gofobao.framework.borrow.repository;

import com.gofobao.framework.borrow.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by Max on 17/5/16.
 */
@Repository
public interface BorrowRepository extends JpaRepository<Borrow,Long> {

    Page<Borrow> findByTypeAndStatusNotIn(Integer type, List<Integer>statusArray, Pageable pageable);

    Page<Borrow> findByAndStatusNotIn(List<Integer>statusArray, Pageable pageable);

    long countByUserIdAndStatusIn(Long userId,List<Integer> statusList);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Borrow findById(Long borrowId);

    List<Borrow>findByIdIn(List<Integer> ids);

}
