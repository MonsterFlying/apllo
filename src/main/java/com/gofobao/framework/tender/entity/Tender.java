package com.gofobao.framework.tender.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.util.ObjectUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
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
    private Long id;
    private Integer type;
    private Integer status;
    private Integer source;
    private Boolean isAuto;
    private Integer autoOrder;
    private Long money;
    private Long validMoney;
    private Integer transferFlag;
    private Date createdAt;
    private Date updatedAt;
    private Long borrowId;
    private Long userId;
    private Integer state;
    private String authCode;
    private Long tUserId;
    private String thirdTenderOrderId;
    private String thirdTransferOrderId;
    private String thirdTenderCancelOrderId;
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
    private Long financeBuyId;
    /**
     * 父级投标id（默认为0，最顶级记录）
     */
    private Long parentId;
    /**
     * 付给债权转让人的当期应计算利息，（债权转让时使用）
     */
    private Long alreadyInterest;

    /**
     * 是否是债权转让投标
     *
     * @return
     */
    public boolean isTransferTender() {
        return !ObjectUtils.isEmpty(parentId) && parentId != 0;
    }

}
