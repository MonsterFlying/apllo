package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by admin on 2017/6/14.
 */
@Data
@ApiModel
public class IndexStatistics {

    @ApiModelProperty("交易总额")
    private Long  transactionsTotal;

    @ApiModelProperty("待收总额")
    private Long dueTotal;

    @ApiModelProperty("注册人数")
    private BigDecimal registerTotal;

    @ApiModelProperty("为用户赚取收益")
    private Long earnings;

    @ApiModelProperty("实现成功贷款（笔）")
    private Long borrowTotal;

    @ApiModelProperty("今日成交额")
    private Long todayDueTotal;

    @ApiModelProperty("昨日成交额")
    private Long yesterdayDueTotal;

    @ApiModelProperty("起头金额(元)")
    private Integer startMoney;

    @ApiModelProperty("年化率")
    private Integer apr;


}
