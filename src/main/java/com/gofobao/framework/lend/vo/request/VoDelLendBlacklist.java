package com.gofobao.framework.lend.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 移除有草出借黑名单视图类
 * Created by Max on 2017/3/31.
 */
@ApiModel("移除有草出借黑名单视图类")
@Data
public class VoDelLendBlacklist {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;

    @ApiModelProperty(name = "blackUserId", value = "黑名单用户id", required = true)
    @NotNull(message = "黑名单用户id不能为空!")
    private Long blackUserId;

}
