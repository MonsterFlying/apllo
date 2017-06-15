package com.gofobao.framework.repayment.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/15.
 */
@Data
public class RepayCollectionLog {

    @ApiModelProperty("期数")
    private Integer order;

    @ApiModelProperty("应还时间")
    private String repayAt;

    @ApiModelProperty("应还金额")
    private String repayMoney;

    @ApiModelProperty("本金")
    private String principal;

    @ApiModelProperty("利息")
    private String interest;

    @ApiModelProperty("实还时间")
    private String repayAtYes;

    @ApiModelProperty("应还金额")
    private String repayMoneyYes;

    @ApiModelProperty("罚息")
    private String lateInterest;

    @ApiModelProperty("备注")
    private String remark;


}
