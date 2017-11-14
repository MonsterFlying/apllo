package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductClassify;
import com.gofobao.framework.product.repository.ProductClassifyRepository;
import com.gofobao.framework.product.service.ProductClassifyService;
import com.gofobao.framework.product.service.ProductClassifyService;
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
public class ProductClassifyServiceImpl implements ProductClassifyService {
    @Autowired
    ProductClassifyRepository productClassifyRepository;

    @Override
    public ProductClassify findById(long id) {
        return productClassifyRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductClassify findByIdLock(long id) {
        return productClassifyRepository.findOne(id);
    }

    @Override
    public List<ProductClassify> findList(Specification<ProductClassify> specification) {
        return productClassifyRepository.findAll(specification);
    }

    @Override
    public List<ProductClassify> findList(Specification<ProductClassify> specification, Sort sort) {
        return productClassifyRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductClassify> findList(Specification<ProductClassify> specification, Pageable pageable) {
        return productClassifyRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductClassify> specification) {
        return productClassifyRepository.count(specification);
    }

    @Override
    public ProductClassify save(ProductClassify productSku) {
        return productClassifyRepository.save(productSku);
    }

    @Override
    public List<ProductClassify> save(List<ProductClassify> productSkuList) {
        return productClassifyRepository.save(productSkuList);
    }
}
