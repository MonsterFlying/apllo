package com.gofobao.framework.member.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/12/15.
 */
@Data
@ApiModel("定时统计")
public class VoCountReq {
    /**
     * 参数json
     */
    @ApiModelProperty("参数json")
    private String paramStr;
    /**
     * 密钥
     */
    @ApiModelProperty("密钥")
    private String sign;

}
