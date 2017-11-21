package com.gofobao.framework.product.repository;

import com.gofobao.framework.product.entity.ProductOrderBuyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/11/14.
 */
@Repository
public interface ProductOrderBuyLogRepository extends JpaRepository<ProductOrderBuyLog, Long>, JpaSpecificationExecutor<ProductOrderBuyLog> {
}
