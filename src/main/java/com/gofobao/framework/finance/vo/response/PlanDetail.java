package com.gofobao.framework.finance.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/8/14.
 */
@Data
public class PlanDetail extends VoBaseResp {
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("理财名")
    private String name;

    @ApiModelProperty("年利率")
    private String apr;

    @ApiModelProperty("投资记录")
    private Integer buyCount;

    @ApiModelProperty("起投金额")
    private String lowMoney;

    @ApiModelProperty("投资总额")
    private String money;

    @ApiModelProperty("剩余总额")
    private String surplusMoney;

    @ApiModelProperty("进度")
    private Double spend;

    @ApiModelProperty("期限: 以月为单位")
    private Integer timeLimit;

    @ApiModelProperty("收益")
    private String earnings;

    @ApiModelProperty("剩余时间")
    private Long surplusSecond;

    @ApiModelProperty("状态：1：抢购；2：售罄; 3：还款中；4：已完成")
    private Integer status;

    @ApiModelProperty("购买截至日")
    private String endAt;

    @ApiModelProperty("购买起始日期")
    private String startAt;

}
