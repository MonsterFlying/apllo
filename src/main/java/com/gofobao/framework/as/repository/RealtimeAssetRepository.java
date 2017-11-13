package com.gofobao.framework.as.repository;

import com.gofobao.framework.as.entity.RealtimeAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RealtimeAssetRepository extends JpaRepository<RealtimeAsset, Long> {
}
