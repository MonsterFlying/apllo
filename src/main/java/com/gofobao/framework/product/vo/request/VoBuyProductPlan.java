package com.gofobao.framework.product.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/11/15.
 */
@Data
@ApiModel
public class VoBuyProductPlan {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty("子商品id")
    @NotNull(message = "商品id不能为空")
    private Long productItemId;

    @ApiModelProperty("广富送计划id")
    @NotNull(message = "广富送计划id不能为空")
    private Long planId;

    @ApiModelProperty("用户地址id")
    @NotNull(message = "用户地址id不能为空")
    private Long addressId;

    @ApiModelProperty("付款方式：0在线支付 默认在线支付")
    private Integer payMode = 0;
}
