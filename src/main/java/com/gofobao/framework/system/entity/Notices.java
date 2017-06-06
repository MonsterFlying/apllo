package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Max on 17/6/5.
 */
@Entity
@Table(name = "gfb_notices")
@Data
public class Notices {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long fromUserId;

    private Long userId;

    private Boolean read;

    private String name;

    private String type;

    private Date createdAt;

    private Date updatedAt;

    private Date deletedAt;

    private String content;
}
