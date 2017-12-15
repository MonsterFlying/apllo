package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/12/6.
 */
@ApiModel("资产详情")
@Data
public class VoAssetDetailResp extends VoBaseResp {
    @ApiModelProperty("可用资产余额")
    private String useMoney;

    @ApiModelProperty("冻结金额")
    private String noUseMoney;

    @ApiModelProperty("待收金额")
    private String waitCollection;

    @ApiModelProperty("待还金额")
    private String waitPayment;

    @ApiModelProperty("净值额度")
    private String netWorthQuota;

    @ApiModelProperty("已实现净收入总额")
    private String netIncomeTotal;

    @ApiModelProperty("未实现净收益总额")
    private String noNetProceeds;

    @ApiModelProperty("总净收益")
    private String sumNetIncome;
}
