package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.LocalRecord;
import com.gofobao.framework.financial.entity.NewEve;
import org.springframework.data.domain.Page;
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


    /**
     * 根据
     * @param beginDate
     * @param endDate
     * @return
     */
    @Query(value = "SELECT " +
            "    log.op_money AS opMoney, " +
            "    log.local_seq_no AS seqNo, " +
            "    log.op_name AS tranName, " +
            "    log.tx_flag AS txFlag, " +
            "    log.platform_type AS tranNo, " +
            "    IFNULL(users.username, '') AS userName, " +
            "    users.phone AS phone, " +
            "    log.create_time AS createTime, " +
            "    account.account_id AS accountId " +
            "FROM" +
            "    gfb_new_asset_log AS log " +
            "        LEFT JOIN " +
            "    gfb_user_third_account AS account ON log.user_id = account.user_id " +
            "        LEFT JOIN " +
            "    gfb_users AS users ON log.user_id = users.id " +
            "WHERE " +
            "    log.create_time < ?2 " +
            "        AND log.create_time > ?1 " +
            "        AND log.del = 0 " +
            "ORDER BY ?#{#pageable} ",
    countQuery = "SELECT " +
            "COUNT(*) " +
            "FROM" +
            "    gfb_new_asset_log AS log " +
            "        LEFT JOIN " +
            "    gfb_user_third_account AS account ON log.user_id = account.user_id " +
            "        LEFT JOIN " +
            "    gfb_users AS users ON log.user_id = users.id " +
            "WHERE " +
            "    log.create_time < ?2 " +
            "        AND log.create_time > ?1 " +
            "        AND log.del = 0 ",
            nativeQuery = true
    )
    Page<Object []> findByCreateTime(String beginDate, String endDate, Pageable pageable) ;


    @Query(value = "SELECT IFNULL(users.`username`, ''), " +
            "       users.`phone`, " +
            "       eve.`cardnbr`, " +
            "       eve.`orderno`, " +
            "       eve.`amount`, " +
            "       eve.`crflag`, " +
            "       eve.`transtype`, " +
            "       eve.ervind," +
            "       eve.`cendt`" +
            "  FROM `gfb_new_eve` AS eve " +
            "  LEFT JOIN `gfb_user_third_account` AS account on account.`account_id`= eve.`cardnbr` " +
            "  LEFT JOIN `gfb_users` AS users on account.`user_id`= users.`id` " +
            " WHERE eve.`query_time`= ?1" +
            " ORDER BY ?#{#pageable} ",
            countQuery = "SELECT  COUNT(eve.id)" +
                    "  FROM `gfb_new_eve` AS eve " +
                    "  LEFT JOIN `gfb_user_third_account` AS account on account.`account_id`= eve.`cardnbr` " +
                    "  LEFT JOIN `gfb_users` AS users on account.`user_id`= users.`id` " +
                    " WHERE eve.`query_time`= ?1",
            nativeQuery = true
    )
    Page<Object[]> findRemoteByQueryTime(String date, Pageable pageable);
}
