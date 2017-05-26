package com.gofobao.framework.borrow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/16.
 */
@Entity
@Table(name = "gfb_borrow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrow implements Serializable {
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "status")
    private Integer status;
    @Basic
    @Column(name = "type")
    private Integer type;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "money")
    private Integer money;
    @Basic
    @Column(name = "repay_fashion")
    private Integer repayFashion;
    @Basic
    @Column(name = "time_limit")
    private Integer timeLimit;
    @Basic
    @Column(name = "apr")
    private Integer apr;
    @Basic
    @Column(name = "lowest")
    private Integer lowest;
    @Basic
    @Column(name = "most")
    private Integer most;
    @Basic
    @Column(name = "most_auto")
    private Integer mostAuto;
    @Basic
    @Column(name = "valid_day")
    private Integer validDay;
    @Basic
    @Column(name = "award_type")
    private Integer awardType;
    @Basic
    @Column(name = "award")
    private Integer award;
    @Basic
    @Column(name = "release_at")
    private Date releaseAt;
    @Basic
    @Column(name = "description")
    private String description;
    @Basic
    @Column(name = "use")
    private Integer use;
    @Basic
    @Column(name = "password_reset")
    private String password;
    @Basic
    @Column(name = "is_lock")
    private Integer isLock;
    @Basic
    @Column(name = "is_vouch")
    private Integer isVouch;
    @Basic
    @Column(name = "is_mortgage")
    private Integer isMortgage;
    @Basic
    @Column(name = "is_impawn")
    private Integer isImpawn;
    @Basic
    @Column(name = "is_continued")
    private Integer isContinued;
    @Basic
    @Column(name = "is_conversion")
    private Integer isConversion;
    @Basic
    @Column(name = "is_novice")
    private Integer isNovice;
    @Basic
    @Column(name = "money_yes")
    private Integer moneyYes;
    @Basic
    @Column(name = "verify_at")
    private Date verifyAt;
    @Basic
    @Column(name = "success_at")
    private Date successAt;
    @Basic
    @Column(name = "close_at")
    private Date closeAt;
    @Basic
    @Column(name = "tender_count")
    private Integer tenderCount;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;

    @Basic
    @Column(name = "t_user_id")
    private Long tUserId;
    @Basic
    @Column(name = "tx_fee")
    private Integer txFee;
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
}
