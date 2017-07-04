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
    @ApiModelProperty("充值渠道：0.江西银行（线上）1.其他")
    private Integer channel;
    @ApiModelProperty("充值金额")
    private String money;
    @ApiModelProperty("充值状态：0：充值请求。1.充值成功。2.充值失败")
    private Integer status;
    @ApiModelProperty("备注")
    private String remark;

}
