package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
public interface ProductItemService {
    /**
     * 根据id查询子商品
     *
     * @param id
     * @return
     */
    ProductItem findById(long id);

    /**
     * 带锁查询子商品
     *
     * @param id
     * @return
     */
    ProductItem findByIdLock(long id);

    /**
     * 查询子商品列表
     *
     * @param specification
     * @return
     */
    List<ProductItem> findList(Specification<ProductItem> specification);

    /**
     * 查询子商品列表
     *
     * @param specification
     * @return
     */
    List<ProductItem> findList(Specification<ProductItem> specification, Sort sort);

    /**
     * 查询子商品列表
     *
     * @param specification
     * @return
     */
    List<ProductItem> findList(Specification<ProductItem> specification, Pageable pageable);

    /**
     * 查询子商品总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductItem> specification);

    /**
     * 保存子商品
     *
     * @param Product
     * @return
     */
    ProductItem save(ProductItem Product);

    /**
     * 保存一组子商品
     *
     * @param ProductList
     * @return
     */
    List<ProductItem> save(List<ProductItem> ProductList);
}
