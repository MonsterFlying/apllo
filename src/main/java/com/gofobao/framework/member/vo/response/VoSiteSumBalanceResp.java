package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by xin on 2017/12/14.
 */
@Data
@ApiModel("网站余额")
public class VoSiteSumBalanceResp extends VoBaseResp {
    @ApiModelProperty("网站余额")
    private Integer siteBalance;

    @ApiModelProperty("截止时间")
    private Date beforeTime;
}
