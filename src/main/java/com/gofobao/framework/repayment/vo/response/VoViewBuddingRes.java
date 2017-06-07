package com.gofobao.framework.repayment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/2.
 */

@Data
@ApiModel("投标中")
public class VoViewBuddingRes {

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("借款期限")
    private String timeLimit;

    @ApiModelProperty("满标进度")
    private String speed;

    @ApiModelProperty("借款金额")
    private String money;

    @ApiModelProperty("标id")
    private Long borrowId;

    @ApiModelProperty("年化率")
    private String apr;


}
