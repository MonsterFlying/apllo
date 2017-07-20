package com.gofobao.framework.member.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * Created by admin on 2017/7/19.
 */
@Data
public class VoApplyForVipReq {

    @ApiModelProperty(hidden = true)
    private Long userId;


    @ApiModelProperty("客服id")
    private Long serviceUserId;

}
