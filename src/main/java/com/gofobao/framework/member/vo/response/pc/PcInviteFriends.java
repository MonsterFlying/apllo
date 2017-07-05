package com.gofobao.framework.member.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/4.
 */
@Data
public class PcInviteFriends {

    @ApiModelProperty("注册时间")
    private String createAt;

    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("是否实名")
    private boolean isRealName;

    @ApiModelProperty("是否投资")
    private boolean isTender;

}
