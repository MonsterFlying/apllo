package com.gofobao.framework.borrow.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/15.
 */
@Data
@ApiModel("借款基本信息")
public class RepaymentBasisInfo {

    @ApiModelProperty("借款金额")
    private String money;

    @ApiModelProperty("期限")
    private String timeLimit;

    @ApiModelProperty("年华利率")
    private String apr;

    @ApiModelProperty("发布时间")
    private String repayAt;

    @ApiModelProperty("截至时间")
    private String endAt;

    @ApiModelProperty("月还本息")
    private String monthAsReimbursement;

    @ApiModelProperty("月还款日")
    private String monthRepaymentAt;
}
