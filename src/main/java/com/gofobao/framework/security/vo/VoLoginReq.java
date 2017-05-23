package com.gofobao.framework.security.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by Max on 2017/5/17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class VoLoginReq implements Serializable{
    @ApiModelProperty(value = "账号")
    @NotNull(message = "账号不能为空！")
    private String account;
    @ApiModelProperty(value = "密码")
    @NotNull(message = "密码不能为空！")
    private String password;
    @ApiModelProperty(value = "图形验证码")
    @NotNull(message = "图形验证码不能为空！")
    private String captcha ;
    @ApiModelProperty(value = "用户来源 0、PC；1、ANDROD；2、IOS 3.h5")
    private Integer source = 0 ;
}
