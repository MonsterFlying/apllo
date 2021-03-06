package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.entity.MarketingRedpackRule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;

@Repository
public interface MarketingRedpackRecordRepository extends JpaRepository<MarketingRedpackRecord, Long>, JpaSpecificationExecutor<MarketingRedpackRecord> {

    MarketingRedpackRule findTopByMarketingIdAndDel(Long marketingId, int del);

    List<MarketingRedpackRecord> findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(Long marketingId, Long redpackRuleId, Long userId, Long sourceId);

    List<MarketingRedpackRecord> findByCancelTimeBetween(Date startDate, Date nowDate, Pageable pageable);

    List<MarketingRedpackRecord> findByUserIdAndState(Long userId, Integer status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    MarketingRedpackRecord findTopByIdAndUserIdAndDel(Long redPackageId, Long userId, int del);

    MarketingRedpackRecord findById(long id);

    List<MarketingRedpackRecord> findByUserIdAndStateAndDel(Long userId, Integer status, int i, Pageable pageable);

    @Query(value = "SELECT  " +
            "    SUM(money) " +
            "FROM " +
            "    gfb_marketing_redpack_record " +
            "WHERE " +
            "    user_id = ?1 AND publish_time > ?2 " +
            "        AND publish_time < ?3 " +
            "        AND state = 1 ", nativeQuery = true)
    Long countByUserIdAndDate(Long userId, Date beginDate, Date nowDate);
}
