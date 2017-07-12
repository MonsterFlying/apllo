package com.gofobao.framework.collection.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "VirtualCollection")
@Data
@Table(name = "gfb_borrow_virtual_collection")
@DynamicInsert
public class VirtualCollection {
    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "status")
    private Integer status;
    @Basic
    @Column(name = "`order`")
    private Integer order;
    @Basic
    @Column(name = "tender_id")
    private Long tenderId;
    @Basic
    @Column(name = "collection_at")
    private Date collectionAt;
    @Basic
    @Column(name = "collection_at_yes")
    private Date collectionAtYes;
    @Basic
    @Column(name = "collection_money")
    private Integer collectionMoney;
    @Basic
    @Column(name = "collection_money_yes")
    private Integer collectionMoneyYes;
    @Basic
    @Column(name = "principal")
    private Integer principal;
    @Basic
    @Column(name = "interest")
    private Integer interest;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;
}
