package com.gofobao.framework.tender.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/16.
 */
@Entity(name = "Tender")
@Table(name = "gfb_borrow_tender")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class Tender {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Column(name = "status")
    private Integer status;
    @Column(name = "source")
    private Integer source;
    @Column(name = "isAuto")
    private Boolean isAuto;
    @Column(name = "auto_order")
    private Integer autoOrder;
    @Column(name = "money")
    private Long money;
    @Column(name = "valid_money")
    private Long validMoney;
    @Column(name = "transfer_flag")
    private Integer transferFlag;
    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;
    @Column(name = "borrow_id")
    private Long borrowId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "state")
    private Integer state;
    @Column(name = "auth_code")
    private String authCode;
    @Column(name = "t_user_id")
    private Long tUserId;
    @Column(name = "third_tender_order_id")
    private String thirdTenderOrderId;
    @Column(name = "third_transfer_order_id")
    private String thirdTransferOrderId;
    private String thirdTenderCancelOrderId;
    private String transferAuthCode;
    private String thirdLendPayOrderId;
    private String thirdCreditEndOrderId;
    private Boolean thirdTransferFlag;
    private Boolean thirdTenderFlag;
    /**
     * 是否在存管进行登记 0否 1.是否
     */
    private Boolean isThirdRegister;
    /**
     * 当前投标记录在是否结束存管债权
     */
    private Boolean thirdCreditEndFlag;
    /**
     * 购买债权记录id
     */
    private Long transferBuyId;
    /**
     * 父级投标id（默认为0，最顶级记录）
     */
    private Long parentId;
    /**
     * 付给债权转让人的当期应计算利息，（债权转让时使用）
     */
    private Long alreadyInterest;
}
