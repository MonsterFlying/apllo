package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/19.
 */
@Data
@ApiModel("复审请求视图类")
public class VoDoAgainVerifyReq {
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
