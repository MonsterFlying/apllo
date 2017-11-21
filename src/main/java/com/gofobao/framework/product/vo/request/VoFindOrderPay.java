package com.gofobao.framework.product.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/11/16.
 */
@ApiModel
@Data
public class VoFindOrderPay {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;
    @ApiModelProperty("订单编号")
    @NotNull(message = "订单编号不能为空!")
    private String orderNumber;
    @ApiModelProperty("支付方式 0在线支付 默认在线支付")
    private Integer payType = 0;
}
