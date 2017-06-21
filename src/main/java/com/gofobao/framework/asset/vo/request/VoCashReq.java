package com.gofobao.framework.asset.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * Created by Administrator on 2017/6/13 0013.
 */
@ApiModel("提现申请")
@Data
public class VoCashReq extends VoBaseReq{
    @ApiModelProperty("提现金额(元)")
    @Min(value = 1)
    private double cashMoney ;

    @ApiModelProperty("联行号,当提现金额大于20万,必须使用人行通道提现")
    private String bankAps ;
}
