package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.BatchAssetChangeItem;
import com.gofobao.framework.asset.repository.BatchAssetChangeItemRepository;
import com.gofobao.framework.asset.service.BatchAssetChangeItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/7/27.
 */
@Service
public class BatchAssetChangeItemServiceImpl implements BatchAssetChangeItemService {

    @Autowired
    private BatchAssetChangeItemRepository batchAssetChangeItemRepository;

    public List<BatchAssetChangeItem> save(List<BatchAssetChangeItem> batchAssetChangeItemList) {
        return batchAssetChangeItemRepository.save(batchAssetChangeItemList);
    }

    public BatchAssetChangeItem save(BatchAssetChangeItem batchAssetChangeItem) {
        return batchAssetChangeItemRepository.save(batchAssetChangeItem);
    }

    public List<BatchAssetChangeItem> findList(Specification<BatchAssetChangeItem> specification) {
        return batchAssetChangeItemRepository.findAll(specification);
    }

    public List<BatchAssetChangeItem> findList(Specification<BatchAssetChangeItem> specification, Pageable pageable) {
        return batchAssetChangeItemRepository.findAll(specification, pageable).getContent();
    }

    public List<BatchAssetChangeItem> findList(Specification<BatchAssetChangeItem> specification, Sort sort) {
        return batchAssetChangeItemRepository.findAll(specification, sort);
    }

    public long count(Specification<BatchAssetChangeItem> specification) {
        return batchAssetChangeItemRepository.count(specification);
    }
}
