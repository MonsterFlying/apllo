package com.gofobao.framework.asset.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/3.
 */
@Data
public class VoPcRechargeReq {

    @ApiModelProperty(hidden = true)
    private Long userId;

    @ApiModelProperty("开始时间")
    private String beginAt;

    @ApiModelProperty("结束时间")
    private String endAt;

    @ApiModelProperty("充值状态：0：充值请求。1.充值成功。2.充值失败'")
    private Integer state;

}

