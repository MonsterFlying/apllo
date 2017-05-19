package com.gofobao.framework.message.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Max on 17/5/19.
 */
@Data
@ApiModel
public class VoUserSmsReq {

    @ApiModelProperty(hidden = true)
    private Long userId ;

    @ApiModelProperty(name = "图形验证码", required = true, dataType = "String" )
    @NotEmpty(message = "图形验证码不能为空")
    private String captcha;

    @NotEmpty
    @ApiModelProperty(name = "captchaToken", required = true, dataType = "String" )
    private String captchaToken ;
}
