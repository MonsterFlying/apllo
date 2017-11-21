package com.gofobao.framework.product.service.impl;

import com.gofobao.framework.product.entity.ProductOrder;
import com.gofobao.framework.product.repository.ProductOrderRepository;
import com.gofobao.framework.product.service.ProductOrderService;
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
public class ProductOrderServiceImpl implements ProductOrderService {
    @Autowired
    ProductOrderRepository productOrderRepository;

    /**
     * 根据订单编号查询数据
     *
     * @param orderNumber
     * @return
     */
    @Override
    public ProductOrder findByOrderNumber(String orderNumber) {
        return productOrderRepository.findByOrderNumber(orderNumber);
    }

    /**
     * 根据订单编号查询数据带锁
     *
     * @param orderNumber
     * @return
     */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public ProductOrder findByOrderNumberLock(String orderNumber) {
        return productOrderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public ProductOrder findById(long id) {
        return productOrderRepository.findOne(id);
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Override
    public ProductOrder findByIdLock(long id) {
        return productOrderRepository.findOne(id);
    }

    @Override
    public List<ProductOrder> findList(Specification<ProductOrder> specification) {
        return productOrderRepository.findAll(specification);
    }

    @Override
    public List<ProductOrder> findList(Specification<ProductOrder> specification, Sort sort) {
        return productOrderRepository.findAll(specification, sort);
    }

    @Override
    public List<ProductOrder> findList(Specification<ProductOrder> specification, Pageable pageable) {
        return productOrderRepository.findAll(specification, pageable).getContent();
    }

    @Override
    public long count(Specification<ProductOrder> specification) {
        return productOrderRepository.count(specification);
    }

    @Override
    public ProductOrder save(ProductOrder productSku) {
        return productOrderRepository.save(productSku);
    }

    @Override
    public List<ProductOrder> save(List<ProductOrder> productSkuList) {
        return productOrderRepository.save(productSkuList);
    }
}
