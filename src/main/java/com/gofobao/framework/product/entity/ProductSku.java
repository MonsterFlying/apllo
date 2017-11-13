package com.gofobao.framework.product.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Zeke on 2017/11/9.
 */
@Entity
@Table(name = "gfb_product_sku")
@Data
public class ProductSku {
    @Id
    @GeneratedValue
    private int id;
    private Integer scId;
    private String name;
    private Boolean isDel;
    private Date createAt;
    private Date updateAt;
}
