package com.gofobao.framework.asset.service;


import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.entity.AssetLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface AssetService {

    /**
     * 根据id产寻资产
     * @param id
     * @return
     */
    Asset findByUserId(Long id);

    List<Asset>findByUserIds(List<Long> userIds);

    Asset findByUserIdLock(Long id);

    Asset save(Asset asset);

    Asset updateById(Asset asset);

    List<Asset> findList(Specification<Asset> specification);

    List<Asset> findList(Specification<Asset> specification, Pageable pageable);

    List<Asset> findList(Specification<Asset> specification, Sort sort);

    long count(Specification<Asset> specification);

    Date findTopOrderByUpdatedAtDesc();
}
