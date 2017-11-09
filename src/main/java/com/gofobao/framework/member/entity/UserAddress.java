package com.gofobao.framework.member.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/11/8.
 */
@Entity
@Table(name = "gfb_user_address")
@Data
@DynamicInsert
@DynamicUpdate
public class UserAddress {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private Integer type;
    private String name;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String district;
    private String detailedAddress;
    private Date createAt;
    private Date updateAt;
    private Boolean isDefault;
    private Boolean isDel;
}
