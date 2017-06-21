package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.CashDetailLog;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Repository
public interface CashDetailLogRepository extends JpaRepository<CashDetailLog, Long> {

    List<CashDetailLog> findByStateInAndUserId(ImmutableList<Integer> states, long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    CashDetailLog findTopBySeqNo(String seqNo);

    List<CashDetailLog> findByUserId(Long userId, Pageable page);

    List<CashDetailLog> findByUserIdAndStateInAndCreateTimeBetween(Long userId, ImmutableList<Integer> stateList, Date startDate, Date endDate);
}
