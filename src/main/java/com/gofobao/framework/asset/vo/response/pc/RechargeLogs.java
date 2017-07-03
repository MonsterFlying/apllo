package com.gofobao.framework.asset.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/3.
 */
@Data
public class RechargeLogs {
    @ApiModelProperty("时间")
    private String createAt;
    @ApiModelProperty("充值渠道")
    private String channel;
    @ApiModelProperty("充值金额")
    private String money;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("备注")
    private String remark;

}
