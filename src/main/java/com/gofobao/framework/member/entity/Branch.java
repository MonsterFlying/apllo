package com.gofobao.framework.member.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by master on 2017/10/17.
 */
@Data
@Entity
@Table(name = "gfb_branch")
public class Branch {
    @Id
    private Long id;

    private Integer type;

    private String name;

    private String phone;

    private String email;

    private String remark;

    private Integer status;

    private Date createdAt;

    private Date updatedAt;

}
