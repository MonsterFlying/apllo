package com.gofobao.framework.asset.repository;

import com.gofobao.framework.asset.entity.YesterdayAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/5/22.
 */
@Repository
public interface YesterdayAssetRepository extends JpaRepository<YesterdayAsset, Long> {
}
