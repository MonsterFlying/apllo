package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.Aleve;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
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
    @Query(value = "select COUNT(cardnbr)  from gfb_aleve  where query_date = ?1 and transtype = ?2 GROUP BY cardnbr", nativeQuery = true)
    Long countOfDateAndTranstype(String date,  String type);


    @Query(value = "select *  FROM gfb_aleve where  query_date = ?1  AND transtype = ?2  GROUP BY cardnbr  ORDER BY ?#{#pageable}",
            countQuery = "select count(id) FROM gfb_aleve where query_date = ?1 and transtype = ?2 GROUP BY cardnbr  ORDER BY ?#{#pageable}",
            nativeQuery =  true)
    Page<Aleve> findBydateQueryAndTranstype(String date, String transtype, Pageable pageable);
}
