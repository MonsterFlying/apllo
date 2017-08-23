package com.gofobao.framework.repayment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/5.
 */
@ApiModel("借款列表")
@Data
public class VoLoanInfo {

    @ApiModelProperty("期数")
    private Integer order;

    @ApiModelProperty("还款日")
    private String repayAt;

    @ApiModelProperty("逾期天数")
    private Integer lateDays;

    @ApiModelProperty("status==0:应还本息; status==1： 已还本息")
    private String repayMoney;

    @ApiModelProperty("status==0:待还本金; status==1 已还本金")
    private String principal;

    @ApiModelProperty("status==0:待还利息; status==1 已还利息 ")
    private String interest;

    @ApiModelProperty("状态")
    private String statusStr;

    @ApiModelProperty("0：还款中，1：已结清")
    private Integer status;

}
