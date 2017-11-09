package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductPlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductPlanService {
    /**
     * 根据id查询商品计划
     *
     * @param id
     * @return
     */
    ProductPlan findById(long id);

    /**
     * 带锁查询商品计划
     *
     * @param id
     * @return
     */
    ProductPlan findByIdLock(long id);

    /**
     * 查询商品计划列表
     *
     * @param specification
     * @return
     */
    List<ProductPlan> findList(Specification<ProductPlan> specification);

    /**
     * 查询商品计划列表
     *
     * @param specification
     * @return
     */
    List<ProductPlan> findList(Specification<ProductPlan> specification, Sort sort);

    /**
     * 查询商品计划列表
     *
     * @param specification
     * @return
     */
    List<ProductPlan> findList(Specification<ProductPlan> specification, Pageable pageable);

    /**
     * 查询商品计划总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductPlan> specification);

    /**
     * 保存商品计划
     *
     * @param Product
     * @return
     */
    ProductPlan save(ProductPlan Product);

    /**
     * 保存一组商品计划
     *
     * @param ProductList
     * @return
     */
    List<ProductPlan> save(List<ProductPlan> ProductList);
}
