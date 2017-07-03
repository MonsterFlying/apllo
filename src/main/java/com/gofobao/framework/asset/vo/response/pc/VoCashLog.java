package com.gofobao.framework.asset.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/3.
 */
@Data
public class VoCashLog {
    @ApiModelProperty("时间")
    private String createTime;

    @ApiModelProperty("提现银行")
    private String banKName;

    @ApiModelProperty("提现账号")
    private String bankNo;

    @ApiModelProperty("提现金额")
    private String money;

    @ApiModelProperty("手续费")
    private String serviceCharge;



}
