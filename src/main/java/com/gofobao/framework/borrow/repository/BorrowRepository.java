package com.gofobao.framework.borrow.repository;

import com.gofobao.framework.borrow.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by Max on 17/5/16.
 */
@Repository
public interface BorrowRepository extends JpaRepository<Borrow,Long>,JpaSpecificationExecutor<Borrow> {

    /**
     *
     * @param type
     * @param statusArray
     * @param pageable
     * @return
     */
    Page<Borrow> findByTypeAndStatusNotIn(Integer type, List<Integer>statusArray, Pageable pageable);

    /**
     * 全部
     * @param statusArray
     * @param pageable
     * @return
     */
    Page<Borrow> findByStatusNotIn(List<Long>statusArray, Pageable pageable);

    /**
     *
     * @param userId
     * @param statusList
     * @return
     */
    long countByUserIdAndStatusIn(Long userId,List<Integer> statusList);

    /**
     *
     * @param ids
     * @return
     */
    List<Borrow>findByIdIn(List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Borrow findById(Long borrowId);




}
