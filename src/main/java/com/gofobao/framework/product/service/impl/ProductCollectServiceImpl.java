package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductCollect;
import com.gofobao.framework.product.repository.ProductCollectRepository;
import com.gofobao.framework.product.service.ProductCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
@Service
public class ProductCollectServiceImpl implements ProductCollectService {
    @Autowired
    ProductCollectRepository productCollectRepository;

    @Override
    public ProductCollect findById(long id) {
        return productCollectRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductCollect findByIdLock(long id) {
        return productCollectRepository.findOne(id);
    }

    @Override
    public List<ProductCollect> findList(Specification<ProductCollect> specification) {
        return productCollectRepository.findAll(specification);
    }

    @Override
    public List<ProductCollect> findList(Specification<ProductCollect> specification, Sort sort) {
        return productCollectRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductCollect> findList(Specification<ProductCollect> specification, Pageable pageable) {
        return productCollectRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductCollect> specification) {
        return productCollectRepository.count(specification);
    }

    @Override
    public ProductCollect save(ProductCollect productSku) {
        return productCollectRepository.save(productSku);
    }

    @Override
    public List<ProductCollect> save(List<ProductCollect> productSkuList) {
        return productCollectRepository.save(productSkuList);
    }

    @Override
    public void del(ProductCollect productCollect) {
        productCollectRepository.delete(productCollect);
    }
}
