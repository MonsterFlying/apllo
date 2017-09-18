package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.Aleve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AleveRepository extends JpaRepository<Aleve, Long>, JpaSpecificationExecutor<Aleve> {
    List<Aleve> findByTranno(String tranno);

    /**
     * 根据用户
     * @param date
     * @param type
     * @return
     */
    @Query(value = "select COUNT(cardnbr)  from #{#entityName} where transtype = :transtype  and query_date = :date GROUP BY cardnbr")
    Long countOfDateAndTranstype(@Param("date")  String date, @Param("transtype") String type);


    @Query(value = "select cardnbr  from #{#entityName} where transtype = :transtype  and query_date = :date GROUP BY cardnbr ")
    Page<Aleve> findByDateAndTranstype(@Param("date")  String date, @Param("transtype") String transtype, Pageable pageable);
}
