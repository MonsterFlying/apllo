package com.gofobao.framework.repayment.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class VoAdvanceReq {
    private Long repaymentId;
}
