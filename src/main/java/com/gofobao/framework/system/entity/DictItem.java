package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Zeke on 2017/5/22.
 */
@Entity
@Data
@DynamicInsert
@Table(name = "gfb_dict_item")
public class DictItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String aliasCode;

    private Integer del;

    private Timestamp createTime;

    private Timestamp updateTime;

    private Integer createId;

    private Integer updateId;

}
