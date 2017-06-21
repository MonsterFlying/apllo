package com.gofobao.framework.tender.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/6/20.
 */
@ApiModel
@Data
public class VoDelAutoTenderReq {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty(name = "tenderId", value = "投标id", dataType = "int", required = true)
    @NotNull(message = "投标id不能为空!")
    private Long tenderId;
}
