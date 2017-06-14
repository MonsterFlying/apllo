package com.gofobao.framework.asset.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 17/6/6.
 */
@Data
@ApiModel
public class VoRechargeEntityResp{
    @ApiModelProperty("充值流水号")
    private String seqNo ;

    @ApiModelProperty("充值名称")
    private String title ;

    @ApiModelProperty("银行卡名称和银行卡号")
    private String bankNameAndCardNo ;

    @ApiModelProperty("充值金额")
    private String rechargeMoney ;

    @ApiModelProperty("充值时间")
    private String rechargetime ;
}
