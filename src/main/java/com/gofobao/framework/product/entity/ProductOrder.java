package com.gofobao.framework.product.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Zeke on 2017/11/16.
 */
@Entity
@Table(name = "gfb_product_order")
@Data
@DynamicInsert
@DynamicUpdate
public class ProductOrder {
    @Id
    @GeneratedValue
    private Long id;
    private Integer type;
    private Long userId;
    private Integer status;
    private String orderNumber;
    private String payNumber;
    private Integer payType;
    private Long planId;
    private Long productAddressId;
    private Long payMoney;
    private Long planMoney;
    private Long productMoney;
    private Long discountsMoney;
    private Long fee;
    private Long earnings;
    private Boolean isDel;
    private Date payAt;
    private Date createdAt;
    private Date updatedAt;
}
