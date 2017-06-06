package com.gofobao.framework.member.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gofobao.framework.core.vo.VoBaseReq;
import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * Created by Zeke on 2017/5/18.
 */
@Data
@ApiModel
public class VoModifyPasswordReq extends VoBaseReq {
    @ApiModelProperty(name = "当前密码", required = true, dataType = "String" )
    @Pattern(regexp = RegexHelper.REGEX_LOGIN_PASSWORD, message = "当前密码格式验证不通过")
    private String oldPassword;

    @ApiModelProperty(name = "新密码", required = true, dataType = "String" )
    @Pattern(regexp = RegexHelper.REGEX_LOGIN_PASSWORD, message = "新的密码格式验证不通过")
    private String newPassword;

}
