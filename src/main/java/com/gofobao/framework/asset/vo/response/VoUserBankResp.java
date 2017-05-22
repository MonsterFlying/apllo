package com.gofobao.framework.asset.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/22.
 */
@ApiModel
@Data
public class VoUserBankResp {
    @ApiModelProperty("银行卡号")
    private String bankNo;
    @ApiModelProperty("银行卡名称")
    private String bankName;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("是否默认银行卡")
    private boolean def;
    @ApiModelProperty("银行卡标识")
    private Long id;
    @ApiModelProperty("银行卡类型")
    private String type ;
    @ApiModelProperty("最小提现额")
    private Long leastCash;
    @ApiModelProperty("最大提现额")
    private Long mostCash;
}
