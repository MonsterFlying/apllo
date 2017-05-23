package com.gofobao.framework.asset.repository;


import com.gofobao.framework.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Asset findByUserId(Long id);

}