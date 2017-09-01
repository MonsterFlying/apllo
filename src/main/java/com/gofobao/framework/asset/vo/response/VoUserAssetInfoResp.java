package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/19.
 */
@Data
public class VoUserAssetInfoResp extends VoBaseResp{
    @ApiModelProperty("会员可用资金")
    private Long useMoney;

    @ApiModelProperty("会员可用资金展示")
    private String hideUserMoney ;

    @ApiModelProperty("会员冻结资金")
    private Long noUseMoney;

    @ApiModelProperty("会员冻结资金(展示)")
    private String  hideNoUseMoney;

    @ApiModelProperty("会员体验金")
    private Long virtualMoney;

    @ApiModelProperty("会员体验金(展示)")
    private String hideVirtualMoney;

    @ApiModelProperty("待收金额")
    private Long collection;

    @ApiModelProperty("待收金额(展示)")
    private String hideCollection;

    @ApiModelProperty("待还金额")
    private Long payment;

    @ApiModelProperty("待还金额(展示)")
    private String hidePayment;

    @ApiModelProperty("净值额度")
    private Long netWorthQuota;

    @ApiModelProperty("净值额度(展示)")
    private String hideNetWorthQuota;

    @ApiModelProperty("净资产")
    private String netAsset;

}
