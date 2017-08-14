package com.gofobao.framework.finance.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/8/14.
 */
@ApiModel
@Data
public class VoTenderFinancePlan {
    @ApiModelProperty(hidden = true)
    private Long userId;
    @ApiModelProperty("理财计划id")
    private Long financePlanId;
    @ApiModelProperty("购买理财计划金额")
    private Long money;
    @ApiModelProperty
    private String remark;
}
