package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by admin on 2017/8/10.
 */
@Entity(name = "Suggest")
@Table(name = "gfb_suggest")
@DynamicUpdate
@DynamicInsert
@Data
public class Suggest {
    @Id
    @GeneratedValue
    private Long id;

    private String content;

    private Date createdAt;

}
