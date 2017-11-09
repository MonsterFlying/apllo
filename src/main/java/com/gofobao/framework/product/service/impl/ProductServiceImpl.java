package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.Product;
import com.gofobao.framework.product.repository.ProductRepository;
import com.gofobao.framework.product.service.ProductService;
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
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository ProductRepository;

    @Override
    public Product findById(long id) {
        return ProductRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public Product findByIdLock(long id) {
        return ProductRepository.findOne(id);
    }

    @Override
    public List<Product> findList(Specification<Product> specification) {
        return ProductRepository.findAll(specification);
    }

    @Override
    public List<Product> findList(Specification<Product> specification, Sort sort) {
        return ProductRepository.findAll(specification, sort);
    }

    @Override
    public List<Product> findList(Specification<Product> specification, Pageable pageable) {
        return ProductRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<Product> specification) {
        return ProductRepository.count(specification);
    }

    @Override
    public Product save(Product productSku) {
        return ProductRepository.save(productSku);
    }

    @Override
    public List<Product> save(List<Product> productSkuList) {
        return ProductRepository.save(productSkuList);
    }
}
