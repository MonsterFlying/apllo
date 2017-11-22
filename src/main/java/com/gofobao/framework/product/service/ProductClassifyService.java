package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductClassify;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductClassifyService {
    /**
     * 根据id查询商品计划关联
     *
     * @param id
     * @return
     */
    ProductClassify findById(long id);

    /**
     * 带锁查询商品计划关联
     *
     * @param id
     * @return
     */
    ProductClassify findByIdLock(long id);

    /**
     * 查询商品计划关联列表
     *
     * @param specification
     * @return
     */
    List<ProductClassify> findList(Specification<ProductClassify> specification);

    /**
     * 查询商品计划关联列表
     *
     * @param specification
     * @return
     */
    List<ProductClassify> findList(Specification<ProductClassify> specification, Sort sort);

    /**
     * 查询商品计划关联列表
     *
     * @param specification
     * @return
     */
    List<ProductClassify> findList(Specification<ProductClassify> specification, Pageable pageable);

    /**
     * 查询商品计划关联总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductClassify> specification);

    /**
     * 保存商品计划关联
     *
     * @param Product
     * @return
     */
    ProductClassify save(ProductClassify Product);

    /**
     * 保存一组商品计划关联
     *
     * @param ProductList
     * @return
     */
    List<ProductClassify> save(List<ProductClassify> ProductList);
}
