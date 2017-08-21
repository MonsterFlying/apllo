package com.gofobao.framework.asset.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by admin on 2017/8/19.
 */
@Entity
@Table(name = "gfb_lianhanghao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class UnionLineNumber {
    @Id
    @GeneratedValue
    private Long id;

    private String number;

    private Integer bank;

    @Basic
    @Column(name = "bankname")
    private String bankName;

    private String tel;

    private Integer province;

    private Integer city;

    private String address;

    private Date createdAt;

    private Date updatedAt;

}
