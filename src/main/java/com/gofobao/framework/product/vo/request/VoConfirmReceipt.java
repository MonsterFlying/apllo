package com.gofobao.framework.product.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/12/12.
 */
@ApiModel("确认收货")
@Data
public class VoConfirmReceipt {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;
    @ApiModelProperty("订单号")
    private String orderNumber;
}
