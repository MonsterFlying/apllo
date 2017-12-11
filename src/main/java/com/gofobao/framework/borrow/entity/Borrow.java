package com.gofobao.framework.borrow.entity;

import com.gofobao.framework.borrow.contants.BorrowContants;
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

    private Long firstMost;

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

    private Boolean isFinance;

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

    @Column(name = "is_starfire")
    private Boolean isStarFire;

    @Column(name = "is_windmill")
    private Boolean isWindmill;
    //放款即信通信状态 0.未处理 1.处理中 2.处理失败 3.处理成功

    @Column(name = "lend_repay_status")
    private Integer lendRepayStatus;

    @Column(name = "is_contract")
    private Boolean isContract;


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


    /**
     * 标的还款方式
     *
     * @return
     */
    public String getBorrowBackWayStr() {
        Integer repayFashion = this.getRepayFashion();
        String back_way = "";
        if (repayFashion == BorrowContants.REPAY_FASHION_ONCE) {
            back_way = BorrowContants.REPAY_FASHION_ONCE_STR;
        }
        if (repayFashion == BorrowContants.REPAY_FASHION_MONTH) {
            back_way = BorrowContants.REPAY_FASHION_MONTH_STR;
        }
        if (repayFashion == BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL) {
            back_way = BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR;
        }
        return back_way;
    }

    /**
     * 标的类型
     * @return
     */
    public String getBorrowTypeStr() {
        Integer borrowType = this.getType();
        String typeStr = "";
        if (borrowType == BorrowContants.CE_DAI) {
            typeStr = BorrowContants.CE_DAI_STR;
        } else if (borrowType == BorrowContants.JING_ZHI) {
            typeStr = BorrowContants.JING_ZHI_STR;
        } else if (borrowType == BorrowContants.MIAO_BIAO) {
            typeStr = BorrowContants.MIAO_BIAO_STR;
        } else {
            typeStr = BorrowContants.QU_DAO_STR;
        }
        return typeStr;
    }


}
