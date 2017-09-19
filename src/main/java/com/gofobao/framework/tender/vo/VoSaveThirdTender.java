package com.gofobao.framework.tender.vo;

import lombok.Data;

/**
 * Created by Zeke on 2017/9/19.
 */
@Data
public class VoSaveThirdTender {
    /**
     * 存管平台分配的账号
     */
    private String accountId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 交易金额
     */
    private String txAmount;
    /**
     * 标的号
     */
    private String productId;
    /**
     * 是否自动投标 true是 false 否
     */
    private Boolean isAuto;
}
