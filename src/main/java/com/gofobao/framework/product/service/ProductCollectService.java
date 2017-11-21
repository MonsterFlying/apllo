package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.ProductCollect;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductCollectService {
    /**
     * 根据id查询订单商品购买记录
     *
     * @param id
     * @return
     */
    ProductCollect findById(long id);

    /**
     * 带锁查询订单商品购买记录
     *
     * @param id
     * @return
     */
    ProductCollect findByIdLock(long id);

    /**
     * 查询订单商品购买记录列表
     *
     * @param specification
     * @return
     */
    List<ProductCollect> findList(Specification<ProductCollect> specification);

    /**
     * 查询订单商品购买记录列表
     *
     * @param specification
     * @return
     */
    List<ProductCollect> findList(Specification<ProductCollect> specification, Sort sort);

    /**
     * 查询订单商品购买记录列表
     *
     * @param specification
     * @return
     */
    List<ProductCollect> findList(Specification<ProductCollect> specification, Pageable pageable);

    /**
     * 查询订单商品购买记录总数
     *
     * @param specification
     * @return
     */
    long count(Specification<ProductCollect> specification);

    /**
     * 保存订单商品购买记录
     *
     * @param productCollect
     * @return
     */
    ProductCollect save(ProductCollect productCollect);

    /**
     * 保存一组订单商品购买记录
     *
     * @param productCollectList
     * @return
     */
    List<ProductCollect> save(List<ProductCollect> productCollectList);
}
