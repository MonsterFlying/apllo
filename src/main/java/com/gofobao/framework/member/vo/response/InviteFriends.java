package com.gofobao.framework.member.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/6.
 */
@Data
public class InviteFriends {
    @ApiModelProperty("时间")
    private String createdAt;
    @ApiModelProperty("等级")
    private Integer leave;
    @ApiModelProperty("比例")
    private String scale;
    @ApiModelProperty("奖励金额")
    private String money;
    @ApiModelProperty("总待收本金")
    private String waitPrincipalTotal;


}
