package com.gofobao.framework.tender.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/2.
 */
@Data
@ApiModel("回款列表")
public class ReturnedMoney {
    @ApiModelProperty("逾期天数")
    private Integer lateDays;

    @ApiModelProperty("标状态 0:回款中 1:已结清")
    private Integer status;

    @ApiModelProperty("当回款状态为：0:收款日;  当回款状态为：1 ：结清日期")
    private String collectionAt;

    @ApiModelProperty("期数")
    private Integer order;

    @ApiModelProperty("应收本息")
    private String collectionMoney;

    @ApiModelProperty("当回款状态为：0:待收本金;  当回款状态为：1 ：已收本金")
    private String principal;

    @ApiModelProperty("当回款状态为：0：待收利息 ; 当回款状态为：1 ：已收利息")
    private String interest;

}
