package com.gofobao.framework.lend.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/12.
 */
@Data
public class LendInfo {

    @ApiModelProperty("出借人")
    private String userName;

    @ApiModelProperty("剩余金额")
    private String surplusMoney;

    @ApiModelProperty("起借金额")
    private String startMoney;

    @ApiModelProperty("年华利率")
    private String apr;

    @ApiModelProperty("期限")
    private String  timeLimit;

    @ApiModelProperty("还款时间")
    private String collectionAt;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("净值额度")
    private String equityLimit;




}
