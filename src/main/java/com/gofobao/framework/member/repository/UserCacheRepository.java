package com.gofobao.framework.member.repository;

import com.gofobao.framework.comment.vo.response.VoCommonDataStatistic;
import com.gofobao.framework.member.entity.UserCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/19.
 */
@Repository
public interface UserCacheRepository extends JpaRepository<UserCache, Long>, JpaSpecificationExecutor<UserCache> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    UserCache findByUserId(Long userId);

    /**
     * 用户总计收益
     *
     * @return
     */
    @Query("SELECT SUM(incomeInterest" +
            "+incomeAward" +
            "+incomeOverdue" +
            "+incomeIntegralCash" +
            "+incomeBonus+incomeOther) FROM UserCache")
    Long userIncomeTotal();

    List<UserCache> findByUserIdIn(List<Long> userId);

    /**
     * 定时统计净值标待收本金
     *
     * @return
     */
    @Query(value = "SELECT\n" +
            "  sum(t3.principal)        AS sum_principal\n" +
            "FROM gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 ON t1.id = t2.borrow_id\n" +
            "  RIGHT JOIN gfb_borrow_collection t3 ON t2.id = t3.tender_id\n" +
            "  LEFT JOIN gfb_users t4 ON t1.user_id = t4.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at <= ?2 AND t2.status = 1 AND t2.created_at <= ?2 AND t2.transfer_flag <> 2\n" +
            "      AND t3.transfer_flag = 0 AND (t3.status = 0 OR t3.collection_at_yes > now()) ",
            nativeQuery = true)
    Integer findWaitCollectionPrincipal(Integer type, Date endDate);

    /**
     * 定时统计净值标待收利息
     *
     * @return
     */
    @Query(value = "SELECT\n" +
            "  sum(t3.interest)        AS sum_principal\n" +
            "FROM gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 ON t1.id = t2.borrow_id\n" +
            "  RIGHT JOIN gfb_borrow_collection t3 ON t2.id = t3.tender_id\n" +
            "  LEFT JOIN gfb_users t4 ON t1.user_id = t4.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at <= ?2 AND t2.status = 1 AND t2.created_at <= ?2 AND t2.transfer_flag <> 2\n" +
            "      AND t3.transfer_flag = 0 AND (t3.status = 0 OR t3.collection_at_yes > now()) ",
            nativeQuery = true)
    Integer findWaitCollectionInterest(Integer type, Date endDate);

    /**
     * 定时统计净值标待还本金
     *
     * @return
     */
    @Query(value = "SELECT\n" +
            "  sum(t2.principal)   AS sum_principal\n" +
            "FROM gfb_borrow t1\n" +
            "RIGHT JOIN gfb_borrow_repayment t2 ON t1.id = t2.borrow_id\n" +
            "LEFT JOIN gfb_users t3 ON t1.user_id = t3.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at <= ?2 AND (t2.status = 0 OR t2.repay_at_yes >= now())",
            nativeQuery = true)
    Integer findWaitRepaymentPrincipal(Integer type, Date endDate);

    /**
     * 定时统计净值标待还利息
     *
     * @return
     */
    @Query(value = "SELECT\n" +
            "  sum(t2.interest)   AS sum_principal\n" +
            "FROM gfb_borrow t1\n" +
            "RIGHT JOIN gfb_borrow_repayment t2 ON t1.id = t2.borrow_id\n" +
            "LEFT JOIN gfb_users t3 ON t1.user_id = t3.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at <= ?2 AND (t2.status = 0 OR t2.repay_at_yes >= now()) ",
            nativeQuery = true)
    Integer findWaitRepaymentInterest(Integer type, Date endDate);

    /**
     * 定时统计车贷标代收本金
     *
     * @param i
     * @param endDate
     * @return
     */
    @Query(value = "SELECT\n" +
            "t4.branch AS branchId , sum(t3.principal) AS sumPrincipal,\n " +
            "sum(t3.interest) AS sumInterest" +
            "FROM gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 ON t1.id = t2.borrow_id\n" +
            "  RIGHT JOIN gfb_borrow_collection t3 ON t2.id = t3.tender_id\n" +
            "  LEFT JOIN gfb_users t4 ON t1.user_id = t4.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at <= ?2 AND t2.status = 1 AND t2.created_at <= ?2 AND t2.transfer_flag <> 2\n" +
            "      AND t3.transfer_flag = 0 AND (t3.status = 0 OR t3.collection_at_yes > now())\n" +
            "GROUP BY branch",
            nativeQuery = true)
    List<VoCommonDataStatistic> findCarWaitCollectionPrincipal(int i, Date endDate);
/*

    */
/**
     * 定时统计车贷标待收利息
     *
     * @param i
     * @param endDate
     * @return
     *//*

    @Query(value = "SELECT\n" +
            "  sum(t3.interest)      AS sum_principal\n" +
            ",t4.branch\n" +
            "FROM gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 ON t1.id = t2.borrow_id\n" +
            "  RIGHT JOIN gfb_borrow_collection t3 ON t2.id = t3.tender_id\n" +
            "  LEFT JOIN gfb_users t4 ON t1.user_id = t4.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at <= ?2 AND t2.status = 1 AND t2.created_at <= ?2 AND t2.transfer_flag <> 2\n" +
            "      AND t3.transfer_flag = 0 AND (t3.status = 0 OR t3.collection_at_yes > now())\n" +
            "GROUP BY branch",
            nativeQuery = true)
    List<Object[]> findCarWaitCollectionInterest(int i, Date endDate);
*/

    /**
     * 定时统计车贷标待还本金
     *
     * @param i
     * @param endDate
     * @return
     */
    @Query(value = "SELECT\n" +
            "t3.branch AS branch,  sum(t2.principal)   AS sumrincipal\n" +
            "FROM gfb_borrow t1\n" +
            "RIGHT JOIN gfb_borrow_repayment t2 ON t1.id = t2.borrow_id\n" +
            "LEFT JOIN gfb_users t3 ON t1.user_id = t3.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at <= ?2 AND (t2.status = 0 OR t2.repay_at_yes >= now())\n" +
            "GROUP BY  t3.branch ",
            nativeQuery = true)
   List<VoCommonDataStatistic> findCarWaitRepaymentPrincipal(int i, Date endDate);

    /**
     * 定时统计车贷标待还利息
     *
     * @param i
     * @param endDate
     */
    @Query(value = "SELECT\n" +
            " t3.branch\n, sum(t2.interest)   AS sum_principal\n" +
            "FROM gfb_borrow t1\n" +
            "RIGHT JOIN gfb_borrow_repayment t2 ON t1.id = t2.borrow_id\n" +
            "LEFT JOIN gfb_users t3 ON t1.user_id = t3.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at <= ?2 AND (t2.status = 0 OR t2.repay_at_yes >= now())\n" +
            "GROUP BY  t3.branch ",
            nativeQuery = true)
    Map<Integer, Long> findCarWaitRepaymentInterest(int i, Date endDate);

    /**
     * 净值标垫付未收回本金
     *
     * @param i
     * @param endDate
     */
    @Query(value = "select sum(t3.principal)\n" +
            "FROM gfb_borrow t1\n" +
            "RIGHT JOIN gfb_borrow_tender t2 on t1.id = t2.borrow_id\n" +
            "LEFT JOIN gfb_borrow_repayment t3 ON t1.id = t3.borrow_id\n" +
            "WHERE t1.status = 3 AND t1.recheck_at <=?2 AND t2.status = 1 AND t2.created_at <=?2 AND t1.type = ?1 AND t3.advance_at_yes IS NOT NULL AND t3.status = 0",
            nativeQuery = true)
    Integer findAdvancePrincipal(int i, Date endDate);

    /**
     * 净值标垫付未收回利息
     *
     * @param i
     * @param endDate
     */
    @Query(value = "select sum(t3.interest)\n" +
            "FROM gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 on t1.id = t2.borrow_id\n" +
            "  LEFT JOIN gfb_borrow_repayment t3 ON t1.id = t3.borrow_id\n" +
            "WHERE t1.status = 3 AND t1.recheck_at <=?2 AND t2.status = 1 AND t2.created_at <=?2 AND t1.type = ?1 AND t3.advance_at_yes IS NOT NULL AND t3.status = 0",
            nativeQuery = true)
    Integer findAdvanceInterest(int i, Date endDate);

    /**
     * 查询某个时间点所有用户余额
     * @param date
     */
    @Query(value = "SELECT sum(t2.use_money + t2.no_use_money)\n" +
            "FROM\n" +
            "  (\n" +
            "    SELECT\n" +
            "      user_id,\n" +
            "      max(id) AS id\n" +
            "    FROM gfb_new_asset_log\n" +
            "    WHERE create_time < ?1\n" +
            "    GROUP BY user_id\n" +
            "  ) t1\n" +
            "  LEFT JOIN gfb_new_asset_log t2 ON t1.id\n" +
            "\n" +
            "                                    = t2.id\n" +
            "\n" +
            "  LEFT JOIN gfb_users t3 ON t1.user_id = t3.id\n" +
            "\n" +
            "WHERE\n" +
            "  t2.use_money + t2.no_use_money > 0\n" +
            "  AND t3.type <> 'borrower'", nativeQuery = true)
    Integer findUserByDate(Date date);
}
