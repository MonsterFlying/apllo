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
public class VoPreRechargeResp extends VoBaseResp {
    @ApiModelProperty("银行账号后4位")
    private String cardNo;

    @ApiModelProperty("银行名称")
    private String bankName ;

    @ApiModelProperty("每次限额")
    private String timesLimit ;

    @ApiModelProperty("每天限额")
    private String dayLimit ;

    @ApiModelProperty("每日限额")
    private String mouthLimit ;

    @ApiModelProperty("银行logo")
    private String logo ;

    @ApiModelProperty("充值至")
    private String toRechargeBankNo ;
}
