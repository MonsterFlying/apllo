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
public class VoBoughtProductPlan {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty("子商品id")
    @NotNull(message = "商品id不能为空")
    private Long productItemId;
}
