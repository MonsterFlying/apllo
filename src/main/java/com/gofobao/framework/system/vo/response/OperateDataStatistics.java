package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by master on 2017/8/25.
 */
@Data
@ApiModel("运营数据")
public class OperateDataStatistics extends VoBaseResp {

    @ApiModelProperty("交易总额")
    private Long  transactionsTotal;

    @ApiModelProperty("为用户赚取收益")
    private Long earnings;

    @ApiModelProperty("待收总额")
    private Long dueTotal;

    @ApiModelProperty("累计成交笔数")
    private Long borrowTotal;

    @ApiModelProperty("年化率")
    private Integer apr;

    @ApiModelProperty("注册人数")
    private Long registerTotal;

    @ApiModelProperty("累计投资人数")
    private Long tenderNoOfPeople ;
 /* @ApiModelProperty("已还本息")
    private Long settleCapitalTotal;

    @ApiModelProperty("人均累计投资金额")
    private Long averageTenderMoneySum;

    @ApiModelProperty("平均每笔投资金额")
    private Long averageTenderMoney;*/



}
