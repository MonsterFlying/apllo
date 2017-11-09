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
@Table(name = "gfb_product_plan")
@Data
public class ProductPlan {
    @Id
    @GeneratedValue
    private Long id;
    private Integer timeLimit;
    private Long lowest;
    private Integer apr;
    private Boolean isOpen;
    private Date startAt;
    private Date endAt;
    private Boolean isDel;
    private Date createAt;
    private Date updateAt;
}
