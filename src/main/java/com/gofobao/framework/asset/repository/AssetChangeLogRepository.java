package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.AssetChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2017/7/7 0007.
 */
@Repository
public interface AssetChangeLogRepository extends JpaRepository<AssetChangeLog, Long>{
}
