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
    private Integer status;
    private Integer source;
    private Boolean isAuto;
    private Integer autoOrder;
    private Integer money;
    private Integer validMoney;
    private Integer transferFlag;
    private Date createdAt;
    private Date updatedAt;
    private Long borrowId;
    private Long userId;
    private Integer state;
    private String authCode;
    private Integer iparam1;
    private Integer iparam2;
    private Integer iparam3;
    private String vparam1;
    private String vparam2;
    private String vparam3;
    private Long tUserId;
    private String thirdTenderOrderId;
    private String thirdTransferOrderId;
}
