package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.NewEve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
