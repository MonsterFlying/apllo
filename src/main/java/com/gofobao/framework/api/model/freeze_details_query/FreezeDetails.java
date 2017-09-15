package com.gofobao.framework.api.model.freeze_details_query;

import lombok.Data;

/**
 * Created by Zeke on 2017/9/15.
 */
@Data
public class FreezeDetails {
    /**
     * 冻结日期
     */
    private String buyDate;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 交易金额
     */
    private String txAmount;
    /**
     * 1-冻结
     9-解冻
     */
    private String state;
}
