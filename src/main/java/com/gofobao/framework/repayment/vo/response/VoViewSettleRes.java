package com.gofobao.framework.repayment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/2.
 */

@Data
@ApiModel("已结清")
public class VoViewSettleRes {

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("发布时间")
    private String releaseAt;

    @ApiModelProperty("待还利息")
    private String interest;

    @ApiModelProperty("待还本金")
    private String principal;

    @ApiModelProperty("借款金额")
    private String money;

    @ApiModelProperty("标id")
    private Long borrowId;

    @ApiModelProperty("结清时间")
    private String closeAt;


}
