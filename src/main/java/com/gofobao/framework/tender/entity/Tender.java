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
    private Integer money;
    @Column(name = "valid_money")
    private Integer validMoney;
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
    @Column(name = "iparam1")
    private Integer iparam1;
    @Column(name = "iparam2")
    private Integer iparam2;
    @Column(name = "iparam3")
    private Integer iparam3;
    @Column(name = "vparam1")
    private String vparam1;
    @Column(name = "vparam2")
    private String vparam2;
    @Column(name = "vparam3")
    private String vparam3;
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
}
