package com.gofobao.framework.award.repository;

import com.gofobao.framework.award.entity.ActivityRedPacketLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by admin on 2017/6/8.
 */

@Repository
public interface RedPackageLogRepository extends JpaRepository<ActivityRedPacketLog,Long> {
}
