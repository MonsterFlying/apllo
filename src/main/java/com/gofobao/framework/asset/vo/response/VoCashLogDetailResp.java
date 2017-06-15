package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/15 0015.
 */
@ApiModel
@Data
public class VoCashLogDetailResp  extends VoBaseResp{
    @ApiModelProperty("提现金额")
    private String cashMoney ;

    @ApiModelProperty("手续费")
    private String fee ;

    @ApiModelProperty("到账金额")
    private String realCashMoney ;

    @ApiModelProperty("提现银行")
    private String bankNameAndCardNo;

    @ApiModelProperty("提现时间")
    private String cashTime ;

    @ApiModelProperty("银行处理时间")
    private String bankProcessTime ;

    @ApiModelProperty("真实到账时间")
    private String realCashTime ;
}
