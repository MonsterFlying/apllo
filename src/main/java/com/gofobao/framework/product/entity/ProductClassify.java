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
@Table(name = "gfb_product_classify")
@DynamicUpdate
@DynamicInsert
@Data
public class ProductClassify {
    @Id
    @GeneratedValue
    private int id;
    private Integer no;
    private String name;
    private String remark;
    private Integer isDel;
    private Timestamp createAt;
    private Timestamp updateAt;
}
