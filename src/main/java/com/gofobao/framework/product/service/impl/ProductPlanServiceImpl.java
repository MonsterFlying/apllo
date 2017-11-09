package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductPlan;
import com.gofobao.framework.product.repository.ProductPlanRepository;
import com.gofobao.framework.product.service.ProductPlanService;
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
public class ProductPlanServiceImpl implements ProductPlanService {
    @Autowired
    ProductPlanRepository productPlanRepository;

    @Override
    public ProductPlan findById(long id) {
        return productPlanRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductPlan findByIdLock(long id) {
        return productPlanRepository.findOne(id);
    }

    @Override
    public List<ProductPlan> findList(Specification<ProductPlan> specification) {
        return productPlanRepository.findAll(specification);
    }

    @Override
    public List<ProductPlan> findList(Specification<ProductPlan> specification, Sort sort) {
        return productPlanRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductPlan> findList(Specification<ProductPlan> specification, Pageable pageable) {
        return productPlanRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductPlan> specification) {
        return productPlanRepository.count(specification);
    }

    @Override
    public ProductPlan save(ProductPlan productSku) {
        return productPlanRepository.save(productSku);
    }

    @Override
    public List<ProductPlan> save(List<ProductPlan> productSkuList) {
        return productPlanRepository.save(productSkuList);
    }
}
