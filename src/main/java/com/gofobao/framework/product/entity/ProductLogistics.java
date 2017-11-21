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
@Table(name = "gfb_product_logistics")
@Data
@DynamicInsert
@DynamicUpdate
public class ProductLogistics {
    @Id
    @GeneratedValue
    private Long id;
    private String orderNumber;
    private Integer state;
    private Long userId;
    private String expressName;
    private String expressNumber;
    private String name;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String district;
    private String detailedAddress;
    private Date createAt;
    private Date updateAt;
}
