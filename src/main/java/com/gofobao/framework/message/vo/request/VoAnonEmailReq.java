package com.gofobao.framework.message.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 发送邮件
 * Created by Max on 17/5/17.
 */
@Data
@ApiModel
public class VoAnonEmailReq {
    @ApiModelProperty(name = "邮件", required = true, dataType = "String" )
    @Pattern(regexp = RegexHelper.REGEX_EMAIL, message = "邮件")
    private String email ;

    @ApiModelProperty(name = "图形验证码", required = true, dataType = "String" )
    private String captcha;

    @ApiModelProperty(name = "captchaToken", required = true, dataType = "String" )
    private String captchaToken ;
}
