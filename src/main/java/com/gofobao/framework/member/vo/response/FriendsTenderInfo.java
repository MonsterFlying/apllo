package com.gofobao.framework.member.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/7.
 */

@Data
public class FriendsTenderInfo {

    @ApiModelProperty("好友名字")
    private String userName;

    @ApiModelProperty("注册时间")
    private String registerTime;

    @ApiModelProperty("首投时间")
    private String firstTenderTime;
}
