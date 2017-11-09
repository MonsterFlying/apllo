package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductSku;
import com.gofobao.framework.product.entity.ProductSkuClassify;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductSkuClassifyService {
    /**
     * 根据id查询sku
     *
     * @param id
     * @return
     */
    ProductSkuClassify findById(long id);

    /**
     * 带锁查询sku
     *
     * @param id
     * @return
     */
    ProductSkuClassify findByIdLock(long id);

    /**
     * 查询sku列表
     *
     * @param specification
     * @return
     */
    List<ProductSkuClassify> findList(Specification<ProductSkuClassify> specification);

    /**
     * 查询sku列表
     *
     * @param specification
     * @return
     */
    List<ProductSkuClassify> findList(Specification<ProductSkuClassify> specification, Sort sort);

    /**
     * 查询sku列表
     *
     * @param specification
     * @return
     */
    List<ProductSkuClassify> findList(Specification<ProductSkuClassify> specification, Pageable pageable);

    /**
     * 查询sku总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductSkuClassify> specification);

    /**
     * 保存sku
     *
     * @param ProductSku
     * @return
     */
    ProductSkuClassify save(ProductSkuClassify ProductSku);

    /**
     * 保存一组sku
     *
     * @param ProductSkuList
     * @return
     */
    List<ProductSkuClassify> save(List<ProductSkuClassify> ProductSkuList);
}
