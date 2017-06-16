package com.gofobao.framework.asset.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * Created by Administrator on 2017/6/13 0013.
 */
@ApiModel
@Data
public class VoBankApsReq {
    @ApiModelProperty("当前页数,初始化为1")
    @Min(value = 1, message = "当前页数必须从1开始")
    private Long page ;
}
