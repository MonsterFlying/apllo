package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/9/12.
 */
@ApiModel
@Data
public class VoFindRepayStatus {
    @ApiModelProperty("存管动态名")
    private String name;
    @ApiModelProperty("处理时间")
    private String dateStr;
    @ApiModelProperty("处理状态 0待处理 1.未通过 2.已通过")
    private int state;
}
