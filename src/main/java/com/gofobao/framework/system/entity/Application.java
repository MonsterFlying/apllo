package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by master on 2017/10/23.
 */
@Data
@Table(name = "gfb_application")
@Entity(name = "Application")
public class Application {

    @Id
    private Integer id;

    private String name;

    private String sketch;

    private String logo;

    private String qrodeUrl;

    private String aliasName;

    private Integer terminal;

    private Date createdAt;

    private Date updateAt;
}
