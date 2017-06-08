package com.gofobao.framework.repayment.vo.request;

import com.gofobao.framework.borrow.entity.Borrow;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/6/6.
 */
@Data
@ApiModel("立即还款视图类")
public class VoRepayReq {
    @ApiModelProperty( hidden = true)
    private Long userId;
    @ApiModelProperty( value = "借款还款id", dataType = "int", required = true)
    @NotNull(message = "借款还款Id不能为空!")
    private Long repaymentId;
    @ApiModelProperty( value = "利息百分比", dataType = "int", required = true)
    private Double interestPercent;
    @ApiModelProperty( value = "是否是用户主动还款", dataType = "int", required = true)
    private Boolean isUserOpen;

}
