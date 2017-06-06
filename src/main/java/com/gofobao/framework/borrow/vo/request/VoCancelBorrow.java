package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/5/26.
 */
@ApiModel
@Data
public class VoCancelBorrow {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty(name = "borrowId", value = "投标 id", dataType = "Long", required = true)
    @NotNull(message = "投标id不能为空!")
    private Long borrowId;
}
