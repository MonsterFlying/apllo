package com.gofobao.framework.tender.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/2.
 */
@Data
@ApiModel("已回款列表")
public class ReturnedMoney {

    @ApiModelProperty("逾期天数")
    private Integer lateDays;

    @ApiModelProperty("标状态 0:回款中 1:已结清")
    private Integer status;

    @ApiModelProperty("回款日")
    private String collectionAt;

    @ApiModelProperty("期数")
    private Integer order;


    @ApiModelProperty("应收本金")
    private String collectionMoney;

    @ApiModelProperty("当标状态为：1：应收本金 ;当标状态为：2 ：已收本金")
    private String principal;

    @ApiModelProperty("当标状态为：1：应收利息 ;当标状态为：2 ：已收利息")
    private String interest;

}
