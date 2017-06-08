package com.gofobao.framework.award.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/8.
 */
@Data
public class OpenRedPackage {

    @ApiModelProperty("红包金额")
    private Double money;

    @ApiModelProperty(hidden = true)
    private boolean flag;
}
