package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.repository.NewAssetLogRepository;
import com.gofobao.framework.asset.service.NewAssetLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewAssetLogServiceImpl implements NewAssetLogService {

    @Autowired
    NewAssetLogRepository newAssetLogRepository;


    @Override
    public NewAssetLog save(NewAssetLog newAssetLog) {
        return newAssetLogRepository.save(newAssetLog);
    }

    @Override
    public Page<NewAssetLog> findAll(Specification<NewAssetLog> specification, Pageable pageable) {
        return newAssetLogRepository.findAll(specification, pageable);
    }

    public List<NewAssetLog> findAll(Specification<NewAssetLog> specification, Sort sort) {
        return newAssetLogRepository.findAll(specification, sort);
    }

    public List<NewAssetLog> findAll(Specification<NewAssetLog> specification) {
        return newAssetLogRepository.findAll(specification);
    }
    @Override
    public long count(Specification<NewAssetLog> assetLogSpecification) {
        return newAssetLogRepository.count(assetLogSpecification);
    }
}
