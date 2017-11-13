package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductItem;
import com.gofobao.framework.product.repository.ProductItemRepository;
import com.gofobao.framework.product.service.ProductItemService;
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
public class ProductItemServiceImpl implements ProductItemService{
    @Autowired
    ProductItemRepository productItemRepository;

    @Override
    public ProductItem findById(long id) {
        return productItemRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductItem findByIdLock(long id) {
        return productItemRepository.findOne(id);
    }

    @Override
    public List<ProductItem> findList(Specification<ProductItem> specification) {
        return productItemRepository.findAll(specification);
    }

    @Override
    public List<ProductItem> findList(Specification<ProductItem> specification, Sort sort) {
        return productItemRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductItem> findList(Specification<ProductItem> specification, Pageable pageable) {
        return productItemRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductItem> specification) {
        return productItemRepository.count(specification);
    }

    @Override
    public ProductItem save(ProductItem productSku) {
        return productItemRepository.save(productSku);
    }

    @Override
    public List<ProductItem> save(List<ProductItem> productSkuList) {
        return productItemRepository.save(productSkuList);
    }
}
