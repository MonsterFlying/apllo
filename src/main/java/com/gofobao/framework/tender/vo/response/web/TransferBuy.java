package com.gofobao.framework.tender.vo.response.web;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/6.
 */
@Data
public class TransferBuy {

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("债权id")
    private Long transferId;

    @ApiModelProperty("购买本金")
    private String principal;

    @ApiModelProperty("购买时间")
    private String createAt;

}
