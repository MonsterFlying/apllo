package com.gofobao.framework.asset.entity;

/**
 * Created by Administrator on 2017/7/8 0008.
 */

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "gfb_assets_change_log_item")
@Entity
@Data
@DynamicInsert
@DynamicUpdate
public class AssetsChangeLogItem {
    @GeneratedValue
    @Id
    private Long id  ;
    private Long assetsChangeLogId  ;
    private Long userId  ;
    private String accountId  ;
    private Long toUserId  ;
    private String toUserAccountId  ;
    private Long money  ;
    private String txFlag  ;
    private Long currMoney  ;
    private Long refId  ;
    private Long thirdSynState  ;
    private String thirdTxType  ;
    private String thirdReponse  ;
    private String thirdSeqNo  ;
    private Date createAt  ;
    private Date updateAt  ;
    private Integer assetChangType  ;
}
