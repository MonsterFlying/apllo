package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.NewAssetLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface NewAssetLogService {
    NewAssetLog save(NewAssetLog newAssetLog);

    /**
     * 查找资金流水
     * @param specification
     * @param pageable
     * @return
     */
    Page<NewAssetLog> findAll(Specification<NewAssetLog> specification, Pageable pageable);

    /**
     * 查询资金记录
     * @param assetLogSpecification
     * @return
     */
    long count(Specification<NewAssetLog> assetLogSpecification);

}
