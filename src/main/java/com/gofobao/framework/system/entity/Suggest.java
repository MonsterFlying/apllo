package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by admin on 2017/8/10.
 */
@Entity
@DynamicInsert
@Data
@Table(name = "gfb_suggest")
public class Suggest {

    private Long id;

    private Long userId;

    private String content;

    private Date createdAt;

}
