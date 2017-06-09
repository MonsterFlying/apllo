package com.gofobao.framework.repayment.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/6/9.
 */
@ApiModel
@Data
public class VoInstantlyRepaymentReq {
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;
    @ApiModelProperty(name = "repaymentId", value = "借款还款id", dataType = "int", required = true)
    @NotNull(message = "借款还款Id不能为空!")
    private Long repaymentId;
}
