package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.JixinAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JixinAssetRepository extends JpaRepository<JixinAsset, Long>,JpaSpecificationExecutor<JixinAsset> {
    JixinAsset findTopByAccountId(String cardnbr);
}
