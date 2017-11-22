package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductOrderService {

    /**
     * 根据订单编号查询数据带锁
     *
     * @param orderNumber
     * @return
     */
    ProductOrder findByOrderNumberLock(String orderNumber);


    /**
     * 根据订单编号查询数据
     *
     * @param orderNumber
     * @return
     */
    ProductOrder findByOrderNumber(String orderNumber);

    /**
     * 根据id查询配送记录
     *
     * @param id
     * @return
     */
    ProductOrder findById(long id);

    /**
     * 带锁查询配送记录
     *
     * @param id
     * @return
     */
    ProductOrder findByIdLock(long id);

    /**
     * 查询配送记录列表
     *
     * @param specification
     * @return
     */
    List<ProductOrder> findList(Specification<ProductOrder> specification);

    /**
     * 查询配送记录列表
     *
     * @param specification
     * @return
     */
    List<ProductOrder> findList(Specification<ProductOrder> specification, Sort sort);

    /**
     * 查询配送记录列表
     *
     * @param specification
     * @return
     */
    List<ProductOrder> findList(Specification<ProductOrder> specification, Pageable pageable);

    /**
     * 查询配送记录总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductOrder> specification);

    /**
     * 保存配送记录
     *
     * @param Product
     * @return
     */
    ProductOrder save(ProductOrder Product);

    /**
     * 保存一组配送记录
     *
     * @param ProductList
     * @return
     */
    List<ProductOrder> save(List<ProductOrder> ProductList);
}
