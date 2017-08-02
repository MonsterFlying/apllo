package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Zeke on 2017/7/27.
 */
@Entity
@Table(name = "gfb_batch_asset_change", catalog = "")
@DynamicInsert
@DynamicUpdate
@Data
public class BatchAssetChange {
    @Id
    @GeneratedValue
    private Long id;
    private Integer type;
    private Long sourceId;
    private Integer state;
    private String batchNo;

}
