package com.gofobao.framework.award.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/8.
 */
@Data
public class VirtualStatistics {

    @ApiModelProperty("可用")
    private Double available;
    @ApiModelProperty("已用")
    private Double used;
    @ApiModelProperty("收益")
    private Double earnings;



}
