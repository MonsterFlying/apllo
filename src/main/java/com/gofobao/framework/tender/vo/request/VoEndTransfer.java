package com.gofobao.framework.tender.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/8/3.
 */
@Data
@ApiModel
public class VoEndTransfer {
    @ApiModelProperty("债权转让id")
    @NotNull(message = "债权转让id不能为空!")
    private long transferId;
    @ApiModelProperty(hidden = true)
    private long userId;
}
