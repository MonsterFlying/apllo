package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "gfb_find")
public class Find {
    @Id
    @GeneratedValue
    private Long id;
    private Long status;
    private String title;
    private String icon;
    private String url;
    private Long createId;
    private Long updateId;
    private Date createdAt;
    private Date updatedAt;
}
