package com.gofobao.framework.message.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/13.
 */
@Data
public class SmsSettingUpdateReq {

    @ApiModelProperty(hidden = true)
    private Long userId;

    @ApiModelProperty("项目回款 0:关闭,1 ：开启")
    private Boolean receivedRepay;

    @ApiModelProperty("成功借款 0:关闭,1 ：开启")
    private Boolean borrowSuccess;
}
