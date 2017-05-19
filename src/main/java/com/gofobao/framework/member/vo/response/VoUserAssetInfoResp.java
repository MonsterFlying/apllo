package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/19.
 */
@Data
public class VoUserAssetInfoResp extends VoBaseResp{
    @ApiModelProperty("会员可用资金")
    private Integer useMoney;
    @ApiModelProperty("会员冻结资金")
    private Integer noUseMoney;
    @ApiModelProperty("会员体验金")
    private Integer virtualMoney;
    @ApiModelProperty("待收金额")
    private Integer collection;
    @ApiModelProperty("待还金额")
    private Integer payment;
    @ApiModelProperty("净值额度")
    private Integer netWorthQuota;
}
