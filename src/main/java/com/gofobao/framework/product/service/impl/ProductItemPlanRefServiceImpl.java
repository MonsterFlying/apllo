package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductItemPlanRef;
import com.gofobao.framework.product.repository.ProductItemPlanRefRepository;
import com.gofobao.framework.product.service.ProductItemPlanRefService;
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
public class ProductItemPlanRefServiceImpl implements ProductItemPlanRefService {
    @Autowired
    ProductItemPlanRefRepository productItemPlanRefRepository;

    @Override
    public ProductItemPlanRef findById(long id) {
        return productItemPlanRefRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductItemPlanRef findByIdLock(long id) {
        return productItemPlanRefRepository.findOne(id);
    }

    @Override
    public List<ProductItemPlanRef> findList(Specification<ProductItemPlanRef> specification) {
        return productItemPlanRefRepository.findAll(specification);
    }

    @Override
    public List<ProductItemPlanRef> findList(Specification<ProductItemPlanRef> specification, Sort sort) {
        return productItemPlanRefRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductItemPlanRef> findList(Specification<ProductItemPlanRef> specification, Pageable pageable) {
        return productItemPlanRefRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductItemPlanRef> specification) {
        return productItemPlanRefRepository.count(specification);
    }

    @Override
    public ProductItemPlanRef save(ProductItemPlanRef productSku) {
        return productItemPlanRefRepository.save(productSku);
    }

    @Override
    public List<ProductItemPlanRef> save(List<ProductItemPlanRef> productSkuList) {
        return productItemPlanRefRepository.save(productSkuList);
    }
}
