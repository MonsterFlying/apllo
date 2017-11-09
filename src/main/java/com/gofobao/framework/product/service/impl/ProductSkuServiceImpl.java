package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductSku;
import com.gofobao.framework.product.repository.ProductSkuRepository;
import com.gofobao.framework.product.service.ProductSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
@Repository
public class ProductSkuServiceImpl implements ProductSkuService {

    @Autowired
    ProductSkuRepository productSkuRepository;

    @Override
    public ProductSku findById(long id) {
        return productSkuRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductSku findByIdLock(long id) {
        return productSkuRepository.findOne(id);
    }

    @Override
    public List<ProductSku> findList(Specification<ProductSku> specification){
        return productSkuRepository.findAll(specification);
    }

    @Override
    public List<ProductSku> findList(Specification<ProductSku> specification, Sort sort){
        return productSkuRepository.findAll(specification,sort);
    }

    @Override
    public List<ProductSku> findList(Specification<ProductSku> specification, Pageable pageable){
        return productSkuRepository.findAll(specification,pageable).getContent();
    }

    @Override
    public long count(Specification<ProductSku> specification){
        return productSkuRepository.count(specification);
    }

    @Override
    public ProductSku save(ProductSku productSku){
        return productSkuRepository.save(productSku);
    }

    @Override
    public List<ProductSku> save(List<ProductSku> productSkuList){
        return productSkuRepository.save(productSkuList);

    }
}
