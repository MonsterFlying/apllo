package com.gofobao.framework.member.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Created by admin on 2017/7/21.
 */
@Data
public class VoSettingTranPassWord {

    @ApiModelProperty(hidden = true)
    private Long userId;

    @ApiModelProperty("交易密码")
    @NotNull(message = "不能为空")
    @Pattern(regexp ="^[\\w\\W]{6,}$")
    private String tranPassWord;

}
