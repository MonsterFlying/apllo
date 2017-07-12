package com.gofobao.framework.asset.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Administrator on 2017/7/12 0012.
 */
@ApiModel
@Data
public class VoSynAssetsRep {
    @ApiModelProperty(name = "sign", value = "签名")
    private String sign;

    @ApiModelProperty(name = "paramStr", value = "参数json: userId ", required = true)
    @NotNull(message = "参数json不能为空!")
    private String paramStr;
}
