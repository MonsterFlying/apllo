package com.gofobao.framework.member.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/21.
 */
@Data
public class VoRestPayPassWord {

    @ApiModelProperty(hidden = true)
    private Long userId;

    @ApiModelProperty("验证码")
    private String code;

    @ApiModelProperty("新交易密码")
    private String newPayPassWord;

}
