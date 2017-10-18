package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "gfb_new_asset_log")
@Data
@DynamicInsert
@DynamicUpdate
public class NewAssetLog {
    @Id
    @GeneratedValue
    private long id;
    private int type;
    private String opName;
    private long opMoney;
    private long useMoney;
    private long noUseMoney;
    private long userId;
    private long forUserId;
    private String platformType;
    private String localType;
    private String txFlag;
    private String localSeqNo;
    private long currMoney;
    private long sourceId;
    private Date createTime;
    private String remark;
    private String groupOpSeqNo;
    private Integer synState;
    private Integer del;
    private Date synTime;
}
