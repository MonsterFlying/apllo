package com.gofobao.framework.repayment.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/6.
 */
@Data
public class VoCollection {
    @ApiModelProperty("还款id")
    private Long repaymentId;

    @ApiModelProperty("标m名")
    private String borrowName;

    @ApiModelProperty("总期数")
    private Integer timeLimit;

    @ApiModelProperty("当期")
    private Integer order;

    @ApiModelProperty("本金")
    private String principal;

    @ApiModelProperty("利息")
    private String interest;

    @ApiModelProperty("还款时间")
    private String repayAt;

    @ApiModelProperty("标是否是摘草")
    private boolean isLend;

}
