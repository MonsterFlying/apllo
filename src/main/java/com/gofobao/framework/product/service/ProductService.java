package com.gofobao.framework.product.service;

import com.gofobao.framework.product.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/11/9.
 */
public interface ProductService {
    /**
     * 根据id查询商品
     *
     * @param id
     * @return
     */
    Product findById(long id);

    /**
     * 带锁查询商品
     *
     * @param id
     * @return
     */
    Product findByIdLock(long id);

    /**
     * 查询商品列表
     *
     * @param specification
     * @return
     */
    List<Product> findList(Specification<Product> specification);

    /**
     * 查询商品列表
     *
     * @param specification
     * @return
     */
    List<Product> findList(Specification<Product> specification, Sort sort);

    /**
     * 查询商品列表
     *
     * @param specification
     * @return
     */
    List<Product> findList(Specification<Product> specification, Pageable pageable);

    /**
     * 查询商品总数
     *
     * @param specification
     * @return
     */
    long count(Specification<Product> specification);

    /**
     * 保存商品
     *
     * @param Product
     * @return
     */
    Product save(Product Product);

    /**
     * 保存一组商品
     *
     * @param ProductList
     * @return
     */
    List<Product> save(List<Product> ProductList);
}
