package com.gofobao.framework.asset.vo.response.pc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/8/21.
 */
@Data
@ApiModel("资金")
public class VoAssetLog {

    @ApiModelProperty("资金变换类型")
    private String typeName;
    
    @ApiModelProperty("创建时间")
    private String createdAt;

    @ApiModelProperty("金额")
    private String money;

    @ApiModelProperty("显示金额")
    private String showMoney;

}
