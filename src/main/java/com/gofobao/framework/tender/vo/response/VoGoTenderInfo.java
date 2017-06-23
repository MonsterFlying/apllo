package com.gofobao.framework.tender.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 17/3/30.
 */
@ApiModel("马上投标详情")
@Data
public class VoGoTenderInfo extends VoBaseResp{
    @ApiModelProperty("借款信息")
    private String borrowName;

    @ApiModelProperty("年利率")
    private String apr;

    @ApiModelProperty("转让期限")
    private String timeLimit;

    @ApiModelProperty("转让本金（元）")
    private String money;

    @ApiModelProperty("下一个还款日期")
    private String nextRepaymentDate;

    @ApiModelProperty("距下一个还款日")
    private String surplusDate;

    @ApiModelProperty("管理费用（元）")
    private String fee;

    @ApiModelProperty("还款方式")
    private String repayFashionStr;

    @ApiModelProperty("投标记录ID")
    private Long tenderId;
}
