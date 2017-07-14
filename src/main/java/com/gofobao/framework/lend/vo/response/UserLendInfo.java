package com.gofobao.framework.lend.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/13.
 */
@Data
public class UserLendInfo {
    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("年华利率")
    private String apr;

    @ApiModelProperty("出借金额")
    private String lendMoney;

    @ApiModelProperty("剩余金额")
    private String surplusMoney;

    @ApiModelProperty("发布时间")
    private String repayAt;

    @ApiModelProperty("状态描述")
    private String statusStr;

    @ApiModelProperty("状态（0、可借；1、结束）")
    private Integer status;

    @ApiModelProperty("lendId")
    private Long lendId;

    @ApiModelProperty("期限(天)")
    private Integer timeLimit;

    @ApiModelProperty("还款时间")
    private String collectionAt;


}
