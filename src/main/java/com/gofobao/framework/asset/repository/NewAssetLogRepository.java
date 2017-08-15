package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.NewAssetLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface NewAssetLogRepository extends JpaRepository<NewAssetLog, Long>, JpaSpecificationExecutor<NewAssetLog> {
}
