package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by master on 2017/10/23.
 */
@Data
@Table(name = "gfb_application_version")
@Entity(name = "ApplicationVersion")
public class ApplicationVersion {

    @Id
    private Integer id;

    private Integer applicationId;

    private Integer versionId;

    private Integer terminal;

    private String viewVersion;

    private String force;

    private String applicationUrl;

    private String description;

    private Date updateAt;

    private Date createdAt;

}
