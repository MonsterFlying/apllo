package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.CurrentIncomeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrentIncomeLogRepository extends JpaRepository<CurrentIncomeLog, Long> {
    List<CurrentIncomeLog> findBySeqNoAndState(String no, int state);

}
