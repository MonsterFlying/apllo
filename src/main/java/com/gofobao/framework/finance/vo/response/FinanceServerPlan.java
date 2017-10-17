package com.gofobao.framework.finance.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/8/14.
 */
@Data
public class FinanceServerPlan {

    @ApiModelProperty
    private Long id;

    @ApiModelProperty("年华率")
    private String apr;

    @ApiModelProperty("期限")
    private Integer timeLimit;

    @ApiModelProperty("理财名")
    private String planName;

    @ApiModelProperty("投资总额")
    private String money;

    @ApiModelProperty("进度")
    private Double spend;

    @ApiModelProperty("状态：1：抢购；2：售罄; 3：还款中；4：已完成")
    private Integer status;

}
