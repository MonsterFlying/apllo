package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/7/27.
 */
public interface BatchAssetChangeItemService {
    List<BatchAssetChangeItem> save(List<BatchAssetChangeItem> batchAssetChangeItemList);

    BatchAssetChangeItem save(BatchAssetChangeItem batchAssetChangeItem);

    List<BatchAssetChangeItem> findList(Specification<BatchAssetChangeItem> specification);

    List<BatchAssetChangeItem> findList(Specification<BatchAssetChangeItem> specification, Pageable pageable);

    List<BatchAssetChangeItem> findList(Specification<BatchAssetChangeItem> specification, Sort sort);

    long count(Specification<BatchAssetChangeItem> specification);
}
