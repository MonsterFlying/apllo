package com.gofobao.framework.product.repository;

import com.gofobao.framework.product.entity.ProductItem;
import com.gofobao.framework.product.entity.ProductItemSkuRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by master on 2017/10/23.
 */
@Repository
public interface ProductItemSkuRefRepository extends JpaRepository<ProductItemSkuRef, Long>,JpaSpecificationExecutor<ProductItemSkuRef>{

}
