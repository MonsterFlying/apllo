package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductClassifySkuClassifyRef;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductClassifySkuClassifyRefService {
    /**
     * 根据id查询商品分类sku分类关联
     *
     * @param id
     * @return
     */
    ProductClassifySkuClassifyRef findById(long id);

    /**
     * 带锁查询商品分类sku分类关联
     *
     * @param id
     * @return
     */
    ProductClassifySkuClassifyRef findByIdLock(long id);

    /**
     * 查询商品分类sku分类关联列表
     *
     * @param specification
     * @return
     */
    List<ProductClassifySkuClassifyRef> findList(Specification<ProductClassifySkuClassifyRef> specification);

    /**
     * 查询商品分类sku分类关联列表
     *
     * @param specification
     * @return
     */
    List<ProductClassifySkuClassifyRef> findList(Specification<ProductClassifySkuClassifyRef> specification, Sort sort);

    /**
     * 查询商品分类sku分类关联列表
     *
     * @param specification
     * @return
     */
    List<ProductClassifySkuClassifyRef> findList(Specification<ProductClassifySkuClassifyRef> specification, Pageable pageable);

    /**
     * 查询商品分类sku分类关联总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductClassifySkuClassifyRef> specification);

    /**
     * 保存商品分类sku分类关联
     *
     * @param Product
     * @return
     */
    ProductClassifySkuClassifyRef save(ProductClassifySkuClassifyRef Product);

    /**
     * 保存一组商品分类sku分类关联
     *
     * @param ProductList
     * @return
     */
    List<ProductClassifySkuClassifyRef> save(List<ProductClassifySkuClassifyRef> ProductList);
}
