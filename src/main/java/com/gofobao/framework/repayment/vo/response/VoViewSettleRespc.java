package com.gofobao.framework.repayment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/2.
 */

@Data
@ApiModel
public class VoViewSettleRespc {

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("发布时间")
    private String releaseAt;

    @ApiModelProperty("已还利息")
    private String interest;

    @ApiModelProperty("已还本金")
    private String principal;

    @ApiModelProperty("还款总额")
    private String collectionMoneyYes;

    @ApiModelProperty("借款金额")
    private String money;

    @ApiModelProperty("标id")
    private Long borrowId;

    @ApiModelProperty("结清时间")
    private String closeAt;

    @ApiModelProperty("期限")
    private String timeLimit;

    @ApiModelProperty("年利率")
    private String apr;
}
