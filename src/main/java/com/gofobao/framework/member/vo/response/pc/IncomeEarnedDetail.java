package com.gofobao.framework.member.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/12.
 */

@Data
public class IncomeEarnedDetail extends VoBaseResp {

    @ApiModelProperty("已赚收益")
    private String incomeEarned;

    @ApiModelProperty("已转利息")
    private String incomeInterest;

    @ApiModelProperty("已转奖励")
    private String incomeAward;

    @ApiModelProperty("逾期收入")
    private String incomeOverdue;

    @ApiModelProperty("积分折现")
    private String incomeIntegralCash;

    @ApiModelProperty("提成收入（推荐人）")
    private String incomeBonus;

    @ApiModelProperty("其他收入")
    private String incomeOther;

    @ApiModelProperty("待收收益")
    private String  waitIncomeInterest;

    @ApiModelProperty("待收利息")
    private String waitCollectionInterest;



}
