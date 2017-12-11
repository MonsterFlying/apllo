package com.gofobao.framework.contract.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class EntrustEnterReq {

    private Long userId;

    @ApiModelProperty("短信验证吗")
    private String smsCode;

    @ApiModelProperty("合同id")
    private String contractId;

}
