package com.gofobao.framework.contract.vo.request;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * Created by master on 2017/11/14.
 */

@Data
public class EntrustAuthReq {

    @ApiModelProperty("用户名")
    private Long userId;

    @ApiModelProperty("合同模板")
    private String templateId;

    @ApiModelProperty("短信验证码")
    private String smsCode;

}
