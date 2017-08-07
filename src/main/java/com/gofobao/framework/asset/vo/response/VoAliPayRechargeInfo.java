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
public class VoAliPayRechargeInfo extends VoBaseResp{
    @ApiModelProperty("开户银行")
    private String bankName ;

    @ApiModelProperty("收款账号")
    private String name ;

    @ApiModelProperty("账号")
    private String bankCardNo;
}
