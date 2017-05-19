package com.gofobao.framework.member.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Zeke on 2017/5/19.
 */
@Data
public class VoBindSwitchPhoneReq extends VoBaseReq {
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long userId;

    @ApiModelProperty(name = "旧手机短信验证码", required = true, dataType = "String" )
    @NotEmpty(message = "旧手机短信验证码不能为空")
    private String phoneCaptcha;

    @ApiModelProperty(name = "绑定手机号", required = true, dataType = "String" )
    @NotEmpty(message = "绑定手机号不能为空！")
    private String newPhone;

    @ApiModelProperty(name = "新手机短信验证码", required = true, dataType = "String" )
    @NotEmpty(message = "绑定手机短信验证码不能为空！")
    private String newPhoneCaptcha;
}
