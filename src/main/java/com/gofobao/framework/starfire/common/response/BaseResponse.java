package com.gofobao.framework.starfire.common.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/26.
 */
@Data
public abstract  class BaseResponse {

    @ApiModelProperty("查询处理结果")
    private String result;

    @ApiModelProperty("流水号")
    private String serial_num;

    @ApiModelProperty("错误信息")
    private String err_msg="";

}
