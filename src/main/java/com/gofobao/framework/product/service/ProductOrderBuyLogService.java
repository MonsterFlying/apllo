package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductOrderBuyLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductOrderBuyLogService {
    /**
     * 根据id查询订单商品购买记录
     *
     * @param id
     * @return
     */
    ProductOrderBuyLog findById(long id);

    /**
     * 带锁查询订单商品购买记录
     *
     * @param id
     * @return
     */
    ProductOrderBuyLog findByIdLock(long id);

    /**
     * 查询订单商品购买记录列表
     *
     * @param specification
     * @return
     */
    List<ProductOrderBuyLog> findList(Specification<ProductOrderBuyLog> specification);

    /**
     * 查询订单商品购买记录列表
     *
     * @param specification
     * @return
     */
    List<ProductOrderBuyLog> findList(Specification<ProductOrderBuyLog> specification, Sort sort);

    /**
     * 查询订单商品购买记录列表
     *
     * @param specification
     * @return
     */
    List<ProductOrderBuyLog> findList(Specification<ProductOrderBuyLog> specification, Pageable pageable);

    /**
     * 查询订单商品购买记录总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductOrderBuyLog> specification);

    /**
     * 保存订单商品购买记录
     *
     * @param Product
     * @return
     */
    ProductOrderBuyLog save(ProductOrderBuyLog Product);

    /**
     * 保存一组订单商品购买记录
     *
     * @param ProductList
     * @return
     */
    List<ProductOrderBuyLog> save(List<ProductOrderBuyLog> ProductList);
}
