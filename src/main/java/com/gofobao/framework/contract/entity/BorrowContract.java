package com.gofobao.framework.contract.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "gfb_borrow_contract")
@Data
@DynamicInsert
@DynamicUpdate
public class BorrowContract {

    @Id
    @GeneratedValue
    private Long id;

    private Long borrowId;

    private String borrowName;

    private Long userId;

    private Long forUserId;

    private Boolean status;

    private Integer type;

    private String batchNo;

    private Date createdAt;

    private Date updateAt;




}
