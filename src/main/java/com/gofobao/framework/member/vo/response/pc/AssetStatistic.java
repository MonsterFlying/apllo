package com.gofobao.framework.member.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/11.
 */
@Data
public class AssetStatistic {

    @ApiModelProperty("账户总额")
    private String assetTotal;

    @ApiModelProperty("可用余额")
    private String useMoney;

    @ApiModelProperty("冻结金额")
    private String noUseMoney;

    @ApiModelProperty("待还金额")
    private String payment;

    @ApiModelProperty("待收金额")
    private String collection;

    @ApiModelProperty("信用额度")
    private String netWorthLimit;

    @ApiModelProperty("已实现净收益")
    private String netProceeds;

    @ApiModelProperty("未实现净收益")
    private String noNetProceeds;

    @ApiModelProperty("总净收益")
    private String sumNetProceeds;

    @ApiModelProperty("待收明细")
    private PaymentDetails paymentDetails;

    @ApiModelProperty("信用额度明细")
    private NetProceedsDetails jingZhiDetails;

    @ApiModelProperty("总支出")
    private String sumExpend;

    @ApiModelProperty("总收益")
    private String sumEarnings;

}
