package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

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
    private Integer batchAssetChangeId;
    private Integer state;
    private Integer userId;
    private Integer toUserId;
    private Integer money;
    private Integer principal;
    private Integer interest;
    private String asset;
    private String type;
    private String remark;
}
