package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductSku;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by master on 2017/10/23.
 */

public interface ProductSkuService {

    /**
     * 根据id查询sku
     *
     * @param id
     * @return
     */
    ProductSku findById(long id);

    /**
     * 带锁查询sku
     *
     * @param id
     * @return
     */
    ProductSku findByIdLock(long id);

    /**
     * 查询sku列表
     *
     * @param specification
     * @return
     */
    List<ProductSku> findList(Specification<ProductSku> specification);

    /**
     * 查询sku列表
     *
     * @param specification
     * @return
     */
    List<ProductSku> findList(Specification<ProductSku> specification, Sort sort);

    /**
     * 查询sku列表
     *
     * @param specification
     * @return
     */
    List<ProductSku> findList(Specification<ProductSku> specification, Pageable pageable);

    /**
     * 查询sku总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductSku> specification);

    /**
     * 保存sku
     *
     * @param ProductSku
     * @return
     */
    ProductSku save(ProductSku ProductSku);

    /**
     * 保存一组sku
     *
     * @param ProductSkuList
     * @return
     */
    List<ProductSku> save(List<ProductSku> ProductSkuList);
}
