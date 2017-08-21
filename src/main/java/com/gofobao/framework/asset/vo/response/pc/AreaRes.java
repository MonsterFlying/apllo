package com.gofobao.framework.asset.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/8/19.
 */
@Data
public class AreaRes {
    private Integer id;
    private String areaName;
    @ApiModelProperty(hidden = true)
    private Integer pid;
}
