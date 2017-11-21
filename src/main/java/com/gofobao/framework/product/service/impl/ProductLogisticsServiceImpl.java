package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductLogistics;
import com.gofobao.framework.product.repository.ProductLogisticsRepository;
import com.gofobao.framework.product.service.ProductLogisticsService;
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
public class ProductLogisticsServiceImpl implements ProductLogisticsService{
    @Autowired
    ProductLogisticsRepository productLogisticsRepository;

    @Override
    public ProductLogistics findById(long id) {
        return productLogisticsRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductLogistics findByIdLock(long id) {
        return productLogisticsRepository.findOne(id);
    }

    @Override
    public List<ProductLogistics> findList(Specification<ProductLogistics> specification) {
        return productLogisticsRepository.findAll(specification);
    }

    @Override
    public List<ProductLogistics> findList(Specification<ProductLogistics> specification, Sort sort) {
        return productLogisticsRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductLogistics> findList(Specification<ProductLogistics> specification, Pageable pageable) {
        return productLogisticsRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductLogistics> specification) {
        return productLogisticsRepository.count(specification);
    }

    @Override
    public ProductLogistics save(ProductLogistics productSku) {
        return productLogisticsRepository.save(productSku);
    }

    @Override
    public List<ProductLogistics> save(List<ProductLogistics> productSkuList) {
        return productLogisticsRepository.save(productSkuList);
    }
}
