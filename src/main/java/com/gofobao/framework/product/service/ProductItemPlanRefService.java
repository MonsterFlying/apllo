package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductItemPlanRef;
import com.gofobao.framework.product.entity.ProductPlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductItemPlanRefService {
    /**
     * 根据id查询商品计划关联
     *
     * @param id
     * @return
     */
    ProductItemPlanRef findById(long id);

    /**
     * 带锁查询商品计划关联
     *
     * @param id
     * @return
     */
    ProductItemPlanRef findByIdLock(long id);

    /**
     * 查询商品计划关联列表
     *
     * @param specification
     * @return
     */
    List<ProductItemPlanRef> findList(Specification<ProductItemPlanRef> specification);

    /**
     * 查询商品计划关联列表
     *
     * @param specification
     * @return
     */
    List<ProductItemPlanRef> findList(Specification<ProductItemPlanRef> specification, Sort sort);

    /**
     * 查询商品计划关联列表
     *
     * @param specification
     * @return
     */
    List<ProductItemPlanRef> findList(Specification<ProductItemPlanRef> specification, Pageable pageable);

    /**
     * 查询商品计划关联总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductItemPlanRef> specification);

    /**
     * 保存商品计划关联
     *
     * @param Product
     * @return
     */
    ProductItemPlanRef save(ProductItemPlanRef Product);

    /**
     * 保存一组商品计划关联
     *
     * @param ProductList
     * @return
     */
    List<ProductItemPlanRef> save(List<ProductItemPlanRef> ProductList);
}
