package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductOrderBuyLog;
import com.gofobao.framework.product.repository.ProductOrderBuyLogRepository;
import com.gofobao.framework.product.service.ProductOrderBuyLogService;
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
public class ProductOrderBuyLogServiceImpl implements ProductOrderBuyLogService {

    @Autowired
    ProductOrderBuyLogRepository productOrderBuyLogRepository;

    @Override
    public ProductOrderBuyLog findById(long id) {
        return productOrderBuyLogRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductOrderBuyLog findByIdLock(long id) {
        return productOrderBuyLogRepository.findOne(id);
    }

    @Override
    public List<ProductOrderBuyLog> findList(Specification<ProductOrderBuyLog> specification) {
        return productOrderBuyLogRepository.findAll(specification);
    }

    @Override
    public List<ProductOrderBuyLog> findList(Specification<ProductOrderBuyLog> specification, Sort sort) {
        return productOrderBuyLogRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductOrderBuyLog> findList(Specification<ProductOrderBuyLog> specification, Pageable pageable) {
        return productOrderBuyLogRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductOrderBuyLog> specification) {
        return productOrderBuyLogRepository.count(specification);
    }

    @Override
    public ProductOrderBuyLog save(ProductOrderBuyLog productSku) {
        return productOrderBuyLogRepository.save(productSku);
    }

    @Override
    public List<ProductOrderBuyLog> save(List<ProductOrderBuyLog> productSkuList) {
        return productOrderBuyLogRepository.save(productSkuList);
    }
}
