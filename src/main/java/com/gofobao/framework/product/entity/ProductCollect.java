package com.gofobao.framework.product.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Zeke on 2017/11/21.
 */
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "gfb_product_collect")
public class ProductCollect {
    @Id
    @GeneratedValue
    private Long id;
    private Long productItemId;
    private Long userId;
}
