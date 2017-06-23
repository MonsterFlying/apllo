package com.gofobao.framework.lend.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 新增有草出借黑名单视图类
 * Created by Max on 2017/3/31.
 */
@ApiModel("新增有草出借黑名单视图类")
@Data
public class VoAddLendBlacklist {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty(name = "blackUserId", value = "拉黑会员id", required = true)
    @NotNull(message = "拉黑会员id不能为空!")
    private Long blackUserId;

}
