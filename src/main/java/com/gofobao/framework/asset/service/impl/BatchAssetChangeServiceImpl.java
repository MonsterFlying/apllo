package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.BatchAssetChange;
import com.gofobao.framework.asset.repository.BatchAssetChangeRepository;
import com.gofobao.framework.asset.service.BatchAssetChangeService;
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
public class BatchAssetChangeServiceImpl implements BatchAssetChangeService {
    @Autowired
    private BatchAssetChangeRepository batchAssetChangeRepository;

    public List<BatchAssetChange> save(List<BatchAssetChange> batchAssetChangeList) {
        return batchAssetChangeRepository.save(batchAssetChangeList);
    }

    public BatchAssetChange save(BatchAssetChange batchAssetChange) {
        return batchAssetChangeRepository.save(batchAssetChange);
    }

    public List<BatchAssetChange> findList(Specification<BatchAssetChange> specification) {
        return batchAssetChangeRepository.findAll(specification);
    }

    public List<BatchAssetChange> findList(Specification<BatchAssetChange> specification, Pageable pageable) {
        return batchAssetChangeRepository.findAll(specification, pageable).getContent();
    }

    public List<BatchAssetChange> findList(Specification<BatchAssetChange> specification, Sort sort) {
        return batchAssetChangeRepository.findAll(specification, sort);
    }

    public long count(Specification<BatchAssetChange> specification){
        return batchAssetChangeRepository.count(specification);
    }
}
