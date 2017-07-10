package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */
@ApiModel("回款中")
@Data
public class VoViewBackMoney {

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("投标日期")
    private String createdAt;

    @ApiModelProperty("投资金额")
    private String money;

    @ApiModelProperty("待收利息")
    private String interest;

    @ApiModelProperty("待收本金")
    private String principal;

    @ApiModelProperty("待收本息")
    private String collectionMoney;

    @ApiModelProperty("待收期数")
    private Integer order;

    @ApiModelProperty("tenderId")
    private Long tenderId;

}
