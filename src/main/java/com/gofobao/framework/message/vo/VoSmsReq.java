package com.gofobao.framework.message.vo;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * 发送短信验证码
 * Created by Max on 17/5/17.
 */
@Data
@ApiModel
public class VoSmsReq{
    @ApiModelProperty(name = "手机号码", required = true, dataType = "String" )
    @Pattern(regexp = RegexHelper.REGEX_MOBILE_EXACT, message = "手机格式有误")
    private String phone ;

    @ApiModelProperty(name = "图形验证码", required = true, dataType = "String" )
    @NotEmpty(message = "图形验证码不能为空")
    private String captcha;

    @NotEmpty
    @ApiModelProperty(name = "captchaToken", required = true, dataType = "String" )
    private String captchaToken ;
}
