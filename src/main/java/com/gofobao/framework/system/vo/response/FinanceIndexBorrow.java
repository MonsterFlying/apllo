package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class FinanceIndexBorrow {
    @ApiModelProperty("理财计划id")
    private Long planId;
    @ApiModelProperty("标题")
    private String title = "";
    @ApiModelProperty("年化率")
    private String apr = "";
    @ApiModelProperty("期限")
    private String limit = "";
    @ApiModelProperty("起投金额")
    private String startLimit = "";
}
