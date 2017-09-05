package com.gofobao.framework.marketing.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MarketingData implements Serializable {
    /**
     * 营销类型
     */
    private String marketingType;

    /**
     * 触发营销的数据
     */
    private String sourceId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 传输时间
     */
    private String transTime;
}
