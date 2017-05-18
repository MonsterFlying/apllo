package com.gofobao.framework.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图形验证码
 * Created by Max on 17/5/18.
 */
@ApiModel
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoCaptchaImageResp {
    @ApiModelProperty(value = "base64的图形验证码")
    private String imageData ;
    @ApiModelProperty(value = "captchaToken")
    private String captchaToken;
}
