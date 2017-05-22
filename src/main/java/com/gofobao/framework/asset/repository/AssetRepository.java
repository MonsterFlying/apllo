package com.gofobao.framework.asset.repository;


import com.gofobao.framework.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset,Long> {

}
