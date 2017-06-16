package com.gofobao.framework.message.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 发送短信验证码
 * Created by Max on 17/5/17.
 */
@Data
@ApiModel
public class VoAnonSmsReq {
    @ApiModelProperty(name = "手机号码", required = true, dataType = "String" )
    @Pattern(regexp = RegexHelper.REGEX_MOBILE_EXACT, message = "手机格式有误")
    private String phone ;

    @ApiModelProperty(name = "图形验证码", required = true, dataType = "String" )
    private String captcha;

    @ApiModelProperty(name = "captchaToken", required = true, dataType = "String" )
    private String captchaToken ;
}
