package com.gofobao.framework.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class MarketingData implements Serializable {
    /**
     * 营销类型
     */
    private String marketingType;

    /**
     * 触发营销的数据
     */
    private Long sourceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 传输时间
     */
    private Date transTime;

    /**
     * 扩展信息
     */
    private Map<String, String> ext = new HashMap<>();
}
