package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductItemSkuRef;
import com.gofobao.framework.product.repository.ProductItemSkuRefRepository;
import com.gofobao.framework.product.service.ProductItemSkuRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
@Service
public class ProductItemSkuRefServiceImpl implements ProductItemSkuRefService {
    @Autowired
    ProductItemSkuRefRepository productItemSkuRefRepository;

    @Override
    public ProductItemSkuRef findById(long id) {
        return productItemSkuRefRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductItemSkuRef findByIdLock(long id) {
        return productItemSkuRefRepository.findOne(id);
    }

    @Override
    public List<ProductItemSkuRef> findList(Specification<ProductItemSkuRef> specification) {
        return productItemSkuRefRepository.findAll(specification);
    }

    @Override
    public List<ProductItemSkuRef> findList(Specification<ProductItemSkuRef> specification, Sort sort) {
        return productItemSkuRefRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductItemSkuRef> findList(Specification<ProductItemSkuRef> specification, Pageable pageable) {
        return productItemSkuRefRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductItemSkuRef> specification) {
        return productItemSkuRefRepository.count(specification);
    }

    @Override
    public ProductItemSkuRef save(ProductItemSkuRef productSku) {
        return productItemSkuRefRepository.save(productSku);
    }

    @Override
    public List<ProductItemSkuRef> save(List<ProductItemSkuRef> productSkuList) {
        return productItemSkuRefRepository.save(productSkuList);
    }
}

