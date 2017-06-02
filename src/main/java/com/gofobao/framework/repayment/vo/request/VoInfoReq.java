package com.gofobao.framework.repayment.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@Data
@ApiModel("还款详情")
public class VoInfoReq {

    @ApiModelProperty("还款id")
    private String repaymentId;

    @ApiModelProperty(hidden = true)
    private Long userId;
}
