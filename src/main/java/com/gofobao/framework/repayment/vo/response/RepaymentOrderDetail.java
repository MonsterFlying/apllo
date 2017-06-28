package com.gofobao.framework.repayment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@Data
@ApiModel("还款详情")
public class RepaymentOrderDetail {
    @ApiModelProperty("第几期")
    private Integer order;

    @ApiModelProperty("回款日期")
    private String repayAt;

    @ApiModelProperty("逾期天数")
    private Integer lateDays;

    @ApiModelProperty("应还本息")
    private String collectionMoney;

    @ApiModelProperty("应还本金")
    private String principal;

    @ApiModelProperty("应还利息")
    private String interest;

    @ApiModelProperty("项目名")
    private String borrowName;

    @ApiModelProperty("状态描述")
    private String statusStr;

    @ApiModelProperty("还款状态 0未还 1已还")
    private Integer status;


}
