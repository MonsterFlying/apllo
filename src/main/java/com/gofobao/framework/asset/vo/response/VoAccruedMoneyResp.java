package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/12 0012.
 */
@Data
@ApiModel("累计详情")
public class VoAccruedMoneyResp extends VoBaseResp {
    @ApiModelProperty("总收入")
    private String totalIncome;
    @ApiModelProperty("理财利息")
    private String incomeInterest;
    @ApiModelProperty("已赚奖励")
    private String incomeAward;
    @ApiModelProperty("罚息收入")
    private String incomeOverdue;
    @ApiModelProperty("提成）")
    private String incomeBonus;
    @ApiModelProperty("积分折现")
    private String incomeIntegralCash;
    @ApiModelProperty("其他收入")
    private String incomeOther;
}
