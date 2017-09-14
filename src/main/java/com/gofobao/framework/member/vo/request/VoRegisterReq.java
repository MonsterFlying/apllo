package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * Created by Zeke on 2017/5/17.
 */
@ApiModel
@Data
public class VoRegisterReq  extends VoBaseReq {
    @ApiModelProperty(value = "注册来源")
    @NotEmpty(message = "注册渠道不能为空")
    private String source ;

    @ApiModelProperty(value = "注册手机")
    @NotEmpty
    @Pattern(regexp = RegexHelper.REGEX_MOBILE_EXACT, message = "手机格式错误")
    private String phone ;

    @ApiModelProperty(value = "短信验证码")
    @NotEmpty
    private String smsCode ;

    @ApiModelProperty(value = "登录密码")
    @NotEmpty
    @Pattern(regexp = RegexHelper.REGEX_LOGIN_PASSWORD, message = "登录密码格式错误")
    private String password ;

    @ApiModelProperty(value = "用户名")
    private String userName ;

    @ApiModelProperty(value = "推荐码")
    private String inviteCode ;

    @ApiModelProperty(hidden = true)
    private String type = "";

}
