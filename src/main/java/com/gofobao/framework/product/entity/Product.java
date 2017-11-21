package com.gofobao.framework.product.entity;

import lombok.Data;

import javax.persistence.*;
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
    private Long pcId;
    private String name;
    private String title;
    private String imgUrl;
    @Column(name = "q_and_a")
    private String qAndA;
    private String afterSalesService;
    private String details;
    private Boolean isDel;
    private Date createAt;
    private Date updateAt;
}
