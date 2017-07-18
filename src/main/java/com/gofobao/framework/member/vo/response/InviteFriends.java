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
    @ApiModelProperty("奖励年利率")
    private String scale;
    @ApiModelProperty("提成奖励")
    private String money;
    @ApiModelProperty("计算提成的总待收本金")
    private String waitPrincipalTotal;


}
