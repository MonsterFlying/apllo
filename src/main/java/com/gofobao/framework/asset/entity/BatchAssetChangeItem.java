package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Zeke on 2017/7/27.
 */
@Entity
@Table(name = "gfb_batch_asset_change_item", catalog = "")
@DynamicInsert
@DynamicUpdate
@Data
public class BatchAssetChangeItem {
    @Id
    @GeneratedValue
    private Long id;
    private Long batchAssetChangeId;
    private Integer state;
    private Long userId;
    private Long toUserId;
    private Long money;
    private Long principal;
    private Long interest;
    private String asset;
    private String type;
    private String remark;
    private Boolean sendRedPacket;/* 是否需要发放存管红包 */
    private Date createdAt;
    private Date updatedAt;
    /**
     *  引用ID
     */
    private Long sourceId ;
    /**
     * 请求流水
     */
    private String seqNo ;
    private String groupSeqNo ;
}
