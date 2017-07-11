package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/14.
 */
@Data
public class IndexStatistics {

    @ApiModelProperty("交易总额")
    private Long  transactionsTotal;

    @ApiModelProperty("待收总额")
    private Long dueTotal;

    @ApiModelProperty("注册人数")
    private Integer userNum;

    @ApiModelProperty("为用户赚取收益")
    private Long earnings;

    @ApiModelProperty("实现成功贷款（笔）")
    private Long order;

    @ApiModelProperty("今日成交额")
    private Integer todayDueTotal;

    @ApiModelProperty("昨日成交额")
    private Integer yesterdayDueTotal;

}
