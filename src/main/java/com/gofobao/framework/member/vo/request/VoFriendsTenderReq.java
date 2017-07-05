package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/4.
 */
@Data
public class VoFriendsTenderReq extends Page {

    @ApiModelProperty("开始时间")
    private String beginAt;

    @ApiModelProperty("结束时间")
    private String endAt;

    @ApiModelProperty(hidden = true)
    private Long userId;
}
