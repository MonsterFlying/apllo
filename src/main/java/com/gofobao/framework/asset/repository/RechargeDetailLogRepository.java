package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.RechargeDetailLog;
import com.google.common.collect.ImmutableList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;
import java.util.List;

/**
 * Created by Max on 17/6/7.
 */
public interface RechargeDetailLogRepository extends JpaRepository<RechargeDetailLog, Long>,JpaSpecificationExecutor<RechargeDetailLog> {
    RechargeDetailLog findTopBySeqNoAndDel(String seqNo, int del);

    RechargeDetailLog findTopByIdAndDel(Long rechargeId, int del);

    List<RechargeDetailLog> findByUserIdAndDel(Long userId, int del, Pageable pageable);

    List<RechargeDetailLog> findByUserIdAndDelAndStateInAndCreateTimeBetween(long userId, int del, ImmutableList<Integer> stateList, Date startTime, Date endTime);


}
