package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@Data
@ApiModel("已结清")
public class VoViewFinanceSettleRes {

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("投标时间")
    private String createdAt;

    @ApiModelProperty("结清时间")
    private String closeAt;

    @ApiModelProperty("投标金额")
    private String money;

    @ApiModelProperty("投标ID")
    private Long buyerId;

    @ApiModelProperty("已收本金")
    private String principal;

    @ApiModelProperty("已收利息")
    private String interest;

    @ApiModelProperty("shi实收本息")
    private String collectionMoneyYes;

    @ApiModelProperty("备注")
    private String remark;

    private Long planId;



}
