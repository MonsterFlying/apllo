package com.gofobao.framework.product.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Zeke on 2017/11/17.
 */
@Entity
@Table(name = "gfb_product_order_buy_log")
@Data
@DynamicInsert
@DynamicUpdate
public class ProductOrderBuyLog {
    @Id
    @GeneratedValue
    private Long id;
    private Long productItemId;
    private Long productOrderId;
    private Long productMoney;
    private Long discountsMoney;
    private Boolean isDel;
    private Date createdAt;
    private Date updatedAt;
}
