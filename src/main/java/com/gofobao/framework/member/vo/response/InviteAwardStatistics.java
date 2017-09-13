package com.gofobao.framework.member.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/7.
 */
@Data
public class InviteAwardStatistics {
    @ApiModelProperty("累计提成奖励")
    private String sumAward="0";

    @ApiModelProperty("昨日奖励")
    private String yesterdayAward="0";

    @ApiModelProperty("邀请总人数")
    public Integer countNum=0;

    @ApiModelProperty("年华利率")
    private String apr="0.02";

    @ApiModelProperty("好友待收本金")
    private String waitPrincipalTotal="0";

    @ApiModelProperty("邀请码1")
    private String inviteCode1;

    @ApiModelProperty("邀请码2")
    private String inviteCode2;

}
