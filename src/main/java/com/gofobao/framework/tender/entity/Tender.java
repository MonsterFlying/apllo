package com.gofobao.framework.tender.entity;

import com.gofobao.framework.member.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/16.
 */
@Entity
@Table(name = "gfb_borrow_tender")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tender {
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "status")
    private Integer status;
    @Basic
    @Column(name = "source")
    private Integer source;
    @Basic
    @Column(name = "is_auto")
    private byte isAuto;
    @Basic
    @Column(name = "auto_order")
    private Integer autoOrder;
    @Basic
    @Column(name = "money")
    private Integer money;
    @Basic
    @Column(name = "valid_money")
    private Integer validMoney;
    @Basic
    @Column(name = "transfer_flag")
    private Integer transferFlag;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;
    @Basic
    @Column(name = "borrow_id")
    private Integer borrowId;
    @Basic
    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Users user;


    @Basic
    @Column(name = "authCode")
    private String authCode;
    @Basic
    @Column(name = "iparam1")
    private Integer iparam1;
    @Basic
    @Column(name = "iparam2")
    private Integer iparam2;
    @Basic
    @Column(name = "iparam3")
    private Integer iparam3;
    @Basic
    @Column(name = "vparam1")
    private String vparam1;
    @Basic
    @Column(name = "vparam2")
    private String vparam2;
    @Basic
    @Column(name = "vparam3")
    private String vparam3;
    @Basic
    @Column(name = "t_user_id")
    private Integer tUserId;
}
