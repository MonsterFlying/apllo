package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.RechargeDetailLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Max on 17/6/7.
 */
public interface RechargeDetailLogRepository extends JpaRepository<RechargeDetailLog, Long> {
    RechargeDetailLog findTopBySeqNoAndDel(String seqNo, int del);

    RechargeDetailLog findTopByIdAndDel(Long rechargeId, int del);
}
