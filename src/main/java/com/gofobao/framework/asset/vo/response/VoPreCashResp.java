package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 17/6/8.
 */
@Data
@ApiModel
public class VoPreCashResp extends VoBaseResp {
    @ApiModelProperty("银行账号后4位")
    private String cardNo;

    @ApiModelProperty("银行账号后4位")
    private String bankName ;

    @ApiModelProperty("存管账户余额")
    private double useMoney ;

    @ApiModelProperty("免费提现额度金额")
    private double freeCashMoney ;

    @ApiModelProperty("银行logo")
    private String logo ;
}
