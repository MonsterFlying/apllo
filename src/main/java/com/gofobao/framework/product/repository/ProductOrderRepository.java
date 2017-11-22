package com.gofobao.framework.product.repository;

import com.gofobao.framework.product.entity.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by master on 2017/10/23.
 */
@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long>, JpaSpecificationExecutor<ProductOrder> {
    /**
     * 根据订单编号查询数据
     * @param orderNumber
     * @return
     */
    ProductOrder findByOrderNumber(String orderNumber);
}
