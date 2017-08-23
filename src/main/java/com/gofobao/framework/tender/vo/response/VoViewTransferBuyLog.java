package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/8/22.
 */
@ApiModel
@Data
public class VoViewTransferBuyLog {
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("购买债权转让时间")
    private String buyAt;
    @ApiModelProperty("年化利率")
    private String apr;
    @ApiModelProperty("期限")
    private String timeLimit;
    @ApiModelProperty("购买金额")
    private String money;
    @ApiModelProperty("收益")
    private String earning;
    @ApiModelProperty("购买份额")
    private String principal;
    @ApiModelProperty("当期应付利息")
    private String alreadyInterest;
}
