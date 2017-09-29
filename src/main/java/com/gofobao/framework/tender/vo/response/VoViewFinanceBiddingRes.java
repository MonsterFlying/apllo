package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@ApiModel("投标中")
@Data
public class VoViewFinanceBiddingRes {

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("投标日期")
    private String createdAt;

    @ApiModelProperty("投资金额")
    private String money;

    @ApiModelProperty("预期收益")
    private String expectEarnings;

    @ApiModelProperty("期限")
    private String timeLimit;

    @ApiModelProperty("年化利率")
    private String  apr;

    @ApiModelProperty("tenderId")
    private Long tenderId;

    @ApiModelProperty("进度")
    private Double spend;

    private Long planId;

}
