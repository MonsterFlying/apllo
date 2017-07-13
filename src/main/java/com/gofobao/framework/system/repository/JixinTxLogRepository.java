package com.gofobao.framework.system.repository;

import com.gofobao.framework.system.entity.JixinTxLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/7/13 0013.
 */
@Repository
public interface JixinTxLogRepository extends JpaRepository<JixinTxLog, Long> {
}
