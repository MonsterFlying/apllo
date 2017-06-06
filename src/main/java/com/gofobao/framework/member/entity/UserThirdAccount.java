package com.gofobao.framework.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Max on 17/5/22.
 */
@Data
@Entity
@DynamicInsert
@Table(name = "gfb_user_third_account")
@NoArgsConstructor
@AllArgsConstructor
public class UserThirdAccount {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @Basic
    @Column(name = "user_id")
    private Long userId;

    @Basic
    @Column(name = "account_id")
    private String accountId;

    @Basic
    @Column(name = "acct_use")
    private Integer acctUse;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "card_no")
    private String cardNo;

    @Basic
    @Column(name = "id_type")
    private Integer idType;

    @Basic
    @Column(name = "id_no")
    private String idNo;

    @Basic
    @Column(name = "mobile")
    private String mobile;

    @Basic
    @Column(name = "channel")
    private Integer channel;

    @Basic
    @Column(name = "password_state")
    private Integer passwordState;

    @Basic
    @Column(name = "card_no_bind_state")
    private Integer cardNoBindState;

    @Basic
    @Column(name = "create_at")
    private Date createAt;


    @Basic
    @Column(name = "update_at")
    private Date updateAt;

    @Basic
    @Column(name = "create_id")
    private Long createId;
    @Basic
    @Column(name = "update_id")
    private Long updateId;

    @Basic
    @Column(name = "auto_tender_tx_amount")
    private Long autoTenderTxAmount;

    @Basic
    @Column(name = "auto_tender_tot_amount")
    private Long autoTenderTotAmount;

    @Basic
    @Column(name = "auto_tender_order_id")
    private String autoTenderOrderId;

    @Basic
    @Column(name = "auto_transfer_bond_order_id")
    private String autoTransferBondOrderId;

    @Basic
    @Column(name = "del")
    private Integer del;
}
