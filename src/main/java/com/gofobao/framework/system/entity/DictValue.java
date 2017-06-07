package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/22.
 */
@Entity
@DynamicInsert
@Data
@Table(name = "gfb_dict_value")
public class DictValue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long itemId;

    private String value01;

    private String value02;

    private String value03;

    private String value04;

    private String value05;

    private String value06;

    private Date createTime;

    private Date updateTime;

    private Integer createId;

    private Integer updateId;

    private Integer del;

    private String name;

}
