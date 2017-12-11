package com.gofobao.framework.product.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/12/4.
 */
@Data
@ApiModel
public class VoAuditOrder {
    @ApiModelProperty("订单编号")
    @NotNull(message = "订单编号不能为空!")
    private String orderNumber;
}
