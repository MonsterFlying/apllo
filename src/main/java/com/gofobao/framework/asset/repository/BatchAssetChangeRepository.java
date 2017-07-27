package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.BatchAssetChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/7/27.
 */
@Repository
public interface BatchAssetChangeRepository extends JpaRepository<BatchAssetChange, Long>, JpaSpecificationExecutor {
}
