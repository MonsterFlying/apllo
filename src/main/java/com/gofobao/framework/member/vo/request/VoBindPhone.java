package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@Data
@ApiModel
public class VoBindPhone {
    @ApiModelProperty("手机号")
    @Pattern(regexp = RegexHelper.REGEX_MOBILE_EXACT, message = "手机号码不能为空!")
    private String  phone ;


    @ApiModelProperty("短信验证码")
    @Pattern(regexp = RegexHelper.ONLY_IS_NUM, message = "短信验证码格式错误!")
    private String smsCode ;
}
