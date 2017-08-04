package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class NewIndexStatisics {

    @ApiModelProperty("交易总额")
    private String totalTransaction = "0";

    @ApiModelProperty("待收金额")
    private String regsiterCount = "0";

    @ApiModelProperty("安全运营")
    private String  safeOperation = "0";

}
