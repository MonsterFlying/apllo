package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "SysVersion")
@Data
@Table(name = "gfb_sys_version")
public class SysVersion {
    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "TERMINAL")
    private Integer terminal;
    @Basic
    @Column(name = "VEIW_VERSION",length = 10)
    private String viewVersion;
    @Basic
    @Column(name = "VERSION_ID")
    private Integer versionId;
    @Basic
    @Column(name = "DETAILS", length = 1024)
    private String details;
    @Basic
    @Column(name = "RUL", nullable = false, length = 200)
    private String rul;
    @Basic
    @Column(name = "IS_DEL")
    private Integer isDel;
    @Basic
    @Column(name = "CREATE_DATE")
    private Timestamp createDate;
    @Basic
    @Column(name = "UPDATE_DATE")
    private Timestamp updateDate;
    @Basic
    @Column(name = "CREATE_ID")
    private Integer createId;
    @Basic
    @Column(name = "UPDATE_ID")
    private Integer updateId;
    @Basic
    @Column(name = "FORCE")
    private Integer force;

}
