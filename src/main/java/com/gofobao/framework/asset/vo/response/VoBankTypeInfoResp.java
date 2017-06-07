package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 17/6/6.
 */
@Data
@ApiModel
public class VoBankTypeInfoResp extends VoBaseResp{
    @ApiModelProperty("银行名称")
    private String bankName ;
    @ApiModelProperty("银行图标")
    private String bankIcon ;
    @ApiModelProperty("每次限额")
    private String timesLimitMoney ;
    @ApiModelProperty("每日限额")
    private String dayLimitMoney ;
    @ApiModelProperty("每月限额")
    private String monthLimitMonty ;
}
