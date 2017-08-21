package com.gofobao.framework.asset.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by admin on 2017/8/19.
 */

@Entity
@Table(name = "gfb_lianhanghao_area")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class Area {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer  pid;
    private String name;
    private Integer  level;
    private Date createdAt;
    private Date  updatedAt;

}
