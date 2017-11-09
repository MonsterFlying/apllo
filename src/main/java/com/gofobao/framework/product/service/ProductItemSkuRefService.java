package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductItemSkuRef;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductItemSkuRefService {
    /**
     * 根据id查询商品sku关联
     *
     * @param id
     * @return
     */
    ProductItemSkuRef findById(long id);

    /**
     * 带锁查询商品sku关联
     *
     * @param id
     * @return
     */
    ProductItemSkuRef findByIdLock(long id);

    /**
     * 查询商品sku关联列表
     *
     * @param specification
     * @return
     */
    List<ProductItemSkuRef> findList(Specification<ProductItemSkuRef> specification);

    /**
     * 查询商品sku关联列表
     *
     * @param specification
     * @return
     */
    List<ProductItemSkuRef> findList(Specification<ProductItemSkuRef> specification, Sort sort);

    /**
     * 查询商品sku关联列表
     *
     * @param specification
     * @return
     */
    List<ProductItemSkuRef> findList(Specification<ProductItemSkuRef> specification, Pageable pageable);

    /**
     * 查询商品sku关联总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductItemSkuRef> specification);

    /**
     * 保存商品sku关联
     *
     * @param Product
     * @return
     */
    ProductItemSkuRef save(ProductItemSkuRef Product);

    /**
     * 保存一组商品sku关联
     *
     * @param ProductList
     * @return
     */
    List<ProductItemSkuRef> save(List<ProductItemSkuRef> ProductList);
}
