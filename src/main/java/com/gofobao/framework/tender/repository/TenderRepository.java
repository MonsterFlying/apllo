package com.gofobao.framework.tender.repository;

import com.gofobao.framework.tender.entity.Tender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Max on 17/5/16.
 */
@Repository
public interface TenderRepository extends JpaRepository<Tender, Long>, JpaSpecificationExecutor<Tender> {


    @Query("select  tender  from #{#entityName}    tender where tender.userId IN :userArray and tender.status=1 group by tender.userId ORDER BY tender.createdAt ASC")
    List<Tender> findUserFirstTender(@Param(value = "userArray") List<Long> userArray);

    List<Tender> findByBorrowIdAndUserIdIs(Long borrowId, Long userId);

    Page<Tender> findByUserIdAndTypeIsAndStatusIsAndTransferFlagIs(Long userId,Integer type, Integer status, Integer transferFlag, Pageable pa);

    Tender findByAuthCode(String authCode);

    List<Tender> findByIdIn(List<Long> ids);
}
