package com.gofobao.framework.message.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Max on 17/5/17.
 */
@Entity
@Table(name = "gfb_sms_log")
@Data
@NoArgsConstructor
@DynamicInsert
@AllArgsConstructor
public class SmsEntity {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    private String username;

    private String type;

    private String phone;

    private String content;

    private String ext;

    private String stime;

    private String rrid;

    private Integer status;

    private String ip;

    private Date createdAt;
}
