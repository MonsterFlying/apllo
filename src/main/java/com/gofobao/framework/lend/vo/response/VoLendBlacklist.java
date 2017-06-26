package com.gofobao.framework.lend.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 2017/3/31.
 */
@ApiModel
@Data
public class VoLendBlacklist {
    @ApiModelProperty("黑名单用户id")
    private Long blackUserId;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("创建时间")
    private String createAt;
}
