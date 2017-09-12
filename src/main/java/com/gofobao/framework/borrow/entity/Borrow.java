package com.gofobao.framework.borrow.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/16.
 */
@Entity
@Table(name = "gfb_borrow")
@DynamicInsert
@DynamicUpdate
@Data
public class Borrow implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    private Long lendId;

    private Long tenderId;

    private Integer status;

    private Integer type;

    private String name;

    private Long money;

    private Integer repayFashion;

    private Integer timeLimit;

    private Integer apr;

    private Integer lowest;

    private Integer most;

    private Integer mostAuto;

    private Integer validDay;

    private Integer awardType;

    private Integer award;

    private Date releaseAt;

    private String description;

    @Column(name = "`use`")
    private Integer use;

    private String password;

    private Boolean isLock;

    private Boolean isVouch;

    private Boolean isMortgage;

    private Boolean isImpawn;

    private Boolean isContinued;

    private Boolean isConversion;

    private Boolean isNovice;

    private Long moneyYes;

    private Date verifyAt;

    private Date successAt;

    private Date closeAt;

    private Integer tenderCount;

    private Date createdAt;

    private Date updatedAt;

    private Long tUserId;

    private Integer txFee;

    private String bailAccountId;

    private String titularBorrowAccountId;

    private Long takeUserId;

    private String productId;

    private Boolean thirdTransferFlag;


    private Date recheckAt;


    @Column(name = "is_windmill")
    private Boolean isWindmill;
    //放款即信通信状态 0.未处理 1.处理中 2.处理失败 3.处理成功


    @Column(name = "lend_repay_status")
    private Integer lendRepayStatus;

    /**
     * 判断是否是转让标
     *
     * @return
     */
    public boolean isTransfer() {
        return this.type == 3 || ((!ObjectUtils.isEmpty(tenderId)) && (tenderId > 0));
    }

    /**
     * 获取借款总期数
     *
     * @return
     */
    public Integer getTotalOrder() {
        return this.repayFashion == 1 ? 1 : this.getTimeLimit();
    }
}
