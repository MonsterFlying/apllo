package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.NewEve;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对账单
 */
@Repository
public interface NewEveRepository extends JpaRepository<NewEve, Long>, JpaSpecificationExecutor<NewEve> {

    /**
     * 根据流水号查找
     * @param orderno
     * @return
     */
    NewEve findTopByOrderno(String orderno);

    @Query(value = "select COUNT(id)  from gfb_new_eve  where query_date = ?2 and transtype = ?1 GROUP BY cardnbr", nativeQuery = true)
    long countByTranstypeAndQueryTime(String transtype, String date);

    /**
     * 查询流水和日期
     * @param orderno
     * @param date
     * @return
     */
    NewEve findTopByOrdernoAndQueryTime(String orderno, String date);

    /**
     * 查询EVE数据
     * @param cendt
     * @param tranno
     * @return
     */
    NewEve findTopByCendtAndTranno(String cendt, String tranno);

    @Query(value = "select *  FROM gfb_new_aleve where  query_time = ?2  AND transtype = ?1  GROUP BY cardnbr  ORDER BY ?#{#pageable}",
            countQuery = "select count(id) FROM gfb_new_aleve where query_date = ?2 and transtype = ?1 GROUP BY cardnbr",
            nativeQuery =  true)
    List<NewEve> findByTranstypeAndQueryTime(String transtype, String date, Pageable pageable);
}
