package com.gofobao.framework.member.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/12/14.
 */
@Data
@ApiModel("第三方登录请求模型")
public class VoThirdLoginReq {
    /**
     * 签名
     */
    @ApiModelProperty(name = "sign", value = "签名")
    private String sign;
    @ApiModelProperty(name = "paramStr", value = "参数json", required = true)
    @NotNull(message = "参数json不能为空!")
    private String paramStr;
}
