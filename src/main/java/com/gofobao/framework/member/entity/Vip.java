package com.gofobao.framework.member.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@Data
@Entity
@Table(name = "gfb_vip")
public class Vip {
    @Id
    @GeneratedValue
    private Long id ;

    private Long userId ;

    private Long kefuId ;

    private Integer status ;

    private String remark ;

    private Date verifyAt ;

    private Date expireAt ;

    private Date createdAt ;

    private Date updatedAt ;
}
