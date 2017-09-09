package com.gofobao.framework.member.entity;

import com.gofobao.framework.common.qiniu.util.StringUtils;
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

    private Long userId;

    private String accountId;

    private Integer acctUse;

    private String name;

    private String cardNo;

    private Integer idType;

    private String idNo;

    private String mobile;

    private Integer channel;

    private Integer passwordState;

    private Integer cardNoBindState;

    private Date createAt;

    private Date updateAt;

    private Long createId;

    private Long updateId;

    private Long autoTenderTxAmount;

    private Long autoTenderTotAmount;

    private String autoTenderOrderId;

    private String autoTransferBondOrderId;

    private Integer autoTransferState;

    private Integer autoTenderState;

    private Integer del;

    private String bankName;

    private String bankLogo;

    public String getIdNo() {
        if (!StringUtils.isNullOrEmpty(idNo)) {
            return idNo.toUpperCase();
        }
        return idNo;
    }

    public void setIdNo(String idNo) {
        if (!StringUtils.isNullOrEmpty(idNo)) {
            this.idNo = idNo.toUpperCase();
        }

    }
}
