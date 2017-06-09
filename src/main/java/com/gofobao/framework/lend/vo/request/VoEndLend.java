package com.gofobao.framework.lend.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/6/9.
 */
@Data
public class VoEndLend {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;
    @ApiModelProperty(name = "lendId", value = "有草出借Id", dataType = "int", required = true)
    @NotNull(message = "有草出借Id不能为空!")
    private Long lendId;
}
