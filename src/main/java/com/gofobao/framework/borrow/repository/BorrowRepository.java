package com.gofobao.framework.borrow.repository;

import com.gofobao.framework.borrow.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;

/**
 * Created by Max on 17/5/16.
 */
@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long>, JpaSpecificationExecutor<Borrow> {

    /**
     * @param type
     * @param statusArray
     * @param pageable
     * @return
     */
    Page<Borrow> findByTypeAndStatusNotIn(Integer type, List<Integer> statusArray, Pageable pageable);

    /**
     * 全部
     *
     * @param statusArray
     * @param pageable
     * @return
     */
    Page<Borrow> findByStatusNotIn(List<Long> statusArray, Pageable pageable);

    /**
     * @param userId
     * @param statusList
     * @return
     */
    long countByUserIdAndStatusIn(Long userId, List<Integer> statusList);

    /**
     * @param ids
     * @return
     */
    List<Borrow> findByIdIn(List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Borrow findById(Long borrowId);

    Borrow findByProductId(String productId);

    /**
     * 修改投标记录数
     *
     * @param borrowId
     */
    @Modifying
    @Query(value = "update gfb_borrow set tender_count=?1 where id=?2", nativeQuery = true)
    //@Query("update Borrow b set b.tenderCount=?1 where b.id=?2")
    void updateTenderCount(Integer count, Long borrowId);

    /**
     * 借款本金
     */
    @Query(value = "select  IFNULL(SUM(t1.money),0) from gfb_borrow t1 RIGHT JOIN gfb_borrow_tender t2 on t1.id = t2.borrow_id WHERE t1.status = 3 AND t1.created_at >= ?2 AND t1.created_at <= ?3 AND t2.status = 1 AND t2.created_at >= ?2 AND t2.created_at <= ?3 AND t1.type = ?1",
            nativeQuery = true)
    Integer findBorrowPrincipal(Integer type, Date startDate, Date endDate);

    /**
     * 还款本金
     *
     * @param type
     * @param startDate
     * @param endDate   @return
     */
    @Query(value = "SELECT  IFNULL(sum(t2.repay_money),0) from gfb_borrow t1 RIGHT JOIN gfb_borrow_repayment t2 ON t1.id = t2.borrow_id WHERE t1.type = ?1 AND t2.status = 1 AND t1.status = 3 AND t1.created_at >= ?2 AND t1.created_at <= ?3 AND t2.repay_at_yes <= ?3 AND t2.repay_at_yes >= ?2",
            nativeQuery = true)
    Integer findRepaymentPrincipal(Integer type, Date startDate, Date endDate);

    /**
     * 净值标垫付本金
     *
     * @return
     */
    @Query(value = "SELECT IFNULL(sum(t2.advance_money_yes),0) FROM gfb_borrow t1 RIGHT JOIN gfb_borrow_repayment t2 on t1.id = t2.borrow_id where t1.type = ?1 AND t1.status = 3  AND t2.advance_at_yes >= ?2 AND t2.advance_at_yes <= ?3",
            nativeQuery = true)
    Integer findAdvancePrincipal(Integer type, Date startDate, Date endDate);

    /**
     * 净值标垫付后收回本金
     *
     * @return
     */
    @Query(value = "SELECT IFNULL(sum(t2.advance_money_yes),0) from gfb_borrow t1 RIGHT JOIN gfb_borrow_repayment t2 on t1.id = t2.borrow_id WHERE t1.type = ?1 AND t1.status = 3 AND t1.created_at >= ?2 AND t1.created_at <= ?3 AND t2.status = 1 AND t2.repay_at_yes >= ?2 AND t2.repay_at_yes <= ?3",
            nativeQuery = true)
    Integer findAdvanceYesPrincipal(Integer type, Date startDate, Date endDate);

    /**
     * 车贷标借款本金
     *
     * @param i
     * @param startDate
     * @param endDate   @return
     */
    @Query(value = "SELECT sum(t1.money),t3.branch\n" +
            "FROM  gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 on t1.id = t2.borrow_id\n" +
            "  LEFT JOIN gfb_users t3 on t1.user_id = t3.id\n" +
            "WHERE  t1.status = 3 AND t1.created_at >= ?2 AND t1.created_at <= ?3 AND t2.status = 1 AND t2.created_at >= ?2 AND t2.created_at <= ?3 AND t1.type = ?1\n" +
            "GROUP BY t3.branch", nativeQuery = true)
    List<Object[]> findCarBorrowPrincipal(int i, Date startDate, Date endDate);

    /**
     * 车贷标还款本金
     *
     * @return
     */
    @Query(value = "SELECT sum(t3.repay_money),t4.branch FROM gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 on t1.id = t2.borrow_id\n" +
            "  RIGHT JOIN gfb_borrow_repayment t3 ON t1.id = t3.borrow_id\n" +
            "  LEFT JOIN gfb_users t4 ON t1.user_id = t4.id\n" +
            "WHERE t1.status = 3 AND t1.created_at >= ?2 AND t1.created_at <= ?3 AND t2.status = 1 AND t2.created_at >= ?2 AND t2.created_at <= ?3 AND t1.type =?1\n" +
            "GROUP BY t4.branch", nativeQuery = true)
    List<Object[]> findCarRepaymentPrincipal(Integer type, Date startDate, Date endDate);

    /**
     * 车贷标待收本金
     *
     * @param i
     * @param startDate
     * @param endDate
     */
    @Query(value = "SELECT\n" +
            "  sum(t3.principal) AS sum_principal,\n" +
            "  t4.branch\n" +
            "FROM gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 ON t1.id = t2.borrow_id\n" +
            "  RIGHT JOIN gfb_borrow_collection t3 ON t2.id = t3.tender_id\n" +
            "  LEFT JOIN gfb_users t4 ON t1.user_id = t4.id\n" +
            "WHERE t1.type = ?1 AND t1.status = 3 AND t1.recheck_at >= ?2 AND t1.recheck_at <= ?3 AND t2.status = 1 AND t2.created_at >= ?2 AND t2.created_at <= ?3 AND\n" +
            "      t2.transfer_flag <> 2\n" +
            "      AND t3.transfer_flag = 0 AND (t3.status = 0 OR t3.collection_at_yes > now())\n" +
            "GROUP BY branch", nativeQuery = true)
    List<Object[]> findCarWaitCollectionPrincipal(int i, Date startDate, Date endDate);

    /**
     * 净值标待收本金
     *
     * @param i
     * @param startDate
     * @param endDate
     */
    @Query(value = "SELECT sum(t3.principal) AS sum_principal\n" +
            "FROM gfb_borrow t1\n" +
            "  RIGHT JOIN gfb_borrow_tender t2 ON t1.id = t2.borrow_id\n" +
            "  RIGHT JOIN gfb_borrow_collection t3 ON t2.id = t3.tender_id\n" +
            "  LEFT JOIN gfb_users t4 ON t1.user_id = t4.id\n" +
            "WHERE\n" +
            "  t1.type = ?1 AND t1.status = 3 AND t1.recheck_at >= ?2 AND t1.recheck_at <= ?3 AND t2.status = 1 AND t2.created_at >= ?2 AND t2.created_at <= ?3 AND\n" +
            "  t2.transfer_flag <> 2\n" +
            "  AND t3.transfer_flag = 0 AND (t3.status = 0 OR t3.collection_at_yes > now())", nativeQuery = true)
    Integer findNetWaitCollectionPrincipal(int i, Date startDate, Date endDate);
}
