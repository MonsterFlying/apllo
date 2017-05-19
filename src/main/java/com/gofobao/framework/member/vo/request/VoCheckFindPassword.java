package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * Created by Zeke on 2017/5/18.
 */
@Data
public class VoCheckFindPassword {
    @ApiModelProperty(name = "手机号码", required = true, dataType = "String" )
    @Pattern(regexp = RegexHelper.REGEX_MOBILE_EXACT, message = "手机格式有误")
    private String phone ;

    @ApiModelProperty(name = "短信验证码", required = true, dataType = "String" )
    @NotEmpty(message = "短信验证码")
    private String phoneCaptcha;
}
