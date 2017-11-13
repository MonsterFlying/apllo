package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductSkuClassify;
import com.gofobao.framework.product.repository.ProductSkuClassifyRepository;
import com.gofobao.framework.product.service.ProductSkuClassifyService;
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
public class ProductSkuClassifyServiceImpl implements ProductSkuClassifyService {

    @Autowired
    ProductSkuClassifyRepository productSkuClassifyRepository;

    @Override
    public ProductSkuClassify findById(long id) {
        return productSkuClassifyRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductSkuClassify findByIdLock(long id) {
        return productSkuClassifyRepository.findOne(id);
    }

    @Override
    public List<ProductSkuClassify> findList(Specification<ProductSkuClassify> specification) {
        return productSkuClassifyRepository.findAll(specification);
    }

    @Override
    public List<ProductSkuClassify> findList(Specification<ProductSkuClassify> specification, Sort sort) {
        return productSkuClassifyRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductSkuClassify> findList(Specification<ProductSkuClassify> specification, Pageable pageable) {
        return productSkuClassifyRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductSkuClassify> specification) {
        return productSkuClassifyRepository.count(specification);
    }

    @Override
    public ProductSkuClassify save(ProductSkuClassify productSku) {
        return productSkuClassifyRepository.save(productSku);
    }

    @Override
    public List<ProductSkuClassify> save(List<ProductSkuClassify> productSkuList) {
        return productSkuClassifyRepository.save(productSkuList);
    }
}
