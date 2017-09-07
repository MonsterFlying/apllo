package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity()
@Table(name = "gfb_banner")
@Data
public class Banner {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Integer id;

    private Byte status;

    private Integer terminal;

    private String title;

    private String imgurl;

    private String clickurl;

    private Integer order;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
