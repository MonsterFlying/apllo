package com.gofobao.framework.tender.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "VirtualCollection")
@Data
@Table(name = "gfb_borrow_virtual_collection")
public class VirtualCollection {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "status", nullable = false)
    private Integer status;
    @Basic
    @Column(name = "order", nullable = false)
    private Integer order;
    @Basic
    @Column(name = "tender_id", nullable = false)
    private Integer tenderId;
    @Basic
    @Column(name = "collection_at", nullable = true)
    private Timestamp collectionAt;
    @Basic
    @Column(name = "collection_at_yes", nullable = true)
    private Timestamp collectionAtYes;
    @Basic
    @Column(name = "collection_money", nullable = false)
    private Integer collectionMoney;
    @Basic
    @Column(name = "collection_money_yes", nullable = false)
    private Integer collectionMoneyYes;
    @Basic
    @Column(name = "principal", nullable = false)
    private Integer principal;
    @Basic
    @Column(name = "interest", nullable = false)
    private Integer interest;
    @Basic
    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at", nullable = true)
    private Timestamp updatedAt;
}
