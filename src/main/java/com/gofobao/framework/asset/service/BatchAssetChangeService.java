package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.BatchAssetChange;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/7/27.
 */
public interface BatchAssetChangeService {
    List<BatchAssetChange> save(List<BatchAssetChange> batchAssetChangeList);

    BatchAssetChange save(BatchAssetChange batchAssetChange);

    List<BatchAssetChange> findList(Specification<BatchAssetChange> specification);

    List<BatchAssetChange> findList(Specification<BatchAssetChange> specification, Pageable pageable);

    List<BatchAssetChange> findList(Specification<BatchAssetChange> specification, Sort sort);

    long count(Specification<BatchAssetChange> specification);
}
