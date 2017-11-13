package com.gofobao.framework.product.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Zeke on 2017/11/9.
 */
@Entity
@Table(name = "gfb_product")
@Data
public class Product {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String title;
    private String afterSalesService;
    private String details;
    private Boolean isDel;
    private Date createAt;
    private Date updateAt;
}
