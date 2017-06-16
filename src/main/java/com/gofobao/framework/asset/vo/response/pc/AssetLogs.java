package com.gofobao.framework.asset.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/16.
 */
@Data
public class AssetLogs {

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("类型名")
    private String typeName;

    @ApiModelProperty("操作金额")
    private String operationMoney;

    @ApiModelProperty("可用金额")
    private String usableMoney;

    @ApiModelProperty("备注")
    private String remark;

}
