package com.gofobao.framework.product.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Zeke on 2017/11/14.
 */
@Entity
@Table(name = "gfb_product_classify_sku_classify_ref")
@Data
public class ProductClassifySkuClassifyRef {
    @Id
    @GeneratedValue
    private Long id;
    private Long pcId;
    private Long scId;
}
