package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductClassifySkuClassifyRef;
import com.gofobao.framework.product.repository.ProductClassifySkuClassifyRefRepository;
import com.gofobao.framework.product.service.ProductClassifySkuClassifyRefService;
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
public class ProductClassifySkuClassifyRefServiceImpl implements ProductClassifySkuClassifyRefService {
    @Autowired
    ProductClassifySkuClassifyRefRepository productClassifySkuClassifyRefRepository;

    @Override
    public ProductClassifySkuClassifyRef findById(long id) {
        return productClassifySkuClassifyRefRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductClassifySkuClassifyRef findByIdLock(long id) {
        return productClassifySkuClassifyRefRepository.findOne(id);
    }

    @Override
    public List<ProductClassifySkuClassifyRef> findList(Specification<ProductClassifySkuClassifyRef> specification) {
        return productClassifySkuClassifyRefRepository.findAll(specification);
    }

    @Override
    public List<ProductClassifySkuClassifyRef> findList(Specification<ProductClassifySkuClassifyRef> specification, Sort sort) {
        return productClassifySkuClassifyRefRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductClassifySkuClassifyRef> findList(Specification<ProductClassifySkuClassifyRef> specification, Pageable pageable) {
        return productClassifySkuClassifyRefRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductClassifySkuClassifyRef> specification) {
        return productClassifySkuClassifyRefRepository.count(specification);
    }

    @Override
    public ProductClassifySkuClassifyRef save(ProductClassifySkuClassifyRef productSku) {
        return productClassifySkuClassifyRefRepository.save(productSku);
    }

    @Override
    public List<ProductClassifySkuClassifyRef> save(List<ProductClassifySkuClassifyRef> productSkuList) {
        return productClassifySkuClassifyRefRepository.save(productSkuList);
    }
}
