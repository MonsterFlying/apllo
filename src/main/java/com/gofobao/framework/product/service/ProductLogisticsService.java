package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductLogistics;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductLogisticsService {
    /**
     * 根据id查询配送记录
     *
     * @param id
     * @return
     */
    ProductLogistics findById(long id);

    /**
     * 带锁查询配送记录
     *
     * @param id
     * @return
     */
    ProductLogistics findByIdLock(long id);

    /**
     * 查询配送记录列表
     *
     * @param specification
     * @return
     */
    List<ProductLogistics> findList(Specification<ProductLogistics> specification);

    /**
     * 查询配送记录列表
     *
     * @param specification
     * @return
     */
    List<ProductLogistics> findList(Specification<ProductLogistics> specification, Sort sort);

    /**
     * 查询配送记录列表
     *
     * @param specification
     * @return
     */
    List<ProductLogistics> findList(Specification<ProductLogistics> specification, Pageable pageable);

    /**
     * 查询配送记录总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductLogistics> specification);

    /**
     * 保存配送记录
     *
     * @param Product
     * @return
     */
    ProductLogistics save(ProductLogistics Product);

    /**
     * 保存一组配送记录
     *
     * @param ProductList
     * @return
     */
    List<ProductLogistics> save(List<ProductLogistics> ProductList);
}
