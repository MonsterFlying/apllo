package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/22.
 */
@ApiModel
@Data
public class VoProductLogistics {
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("时间")
    private String date;
}
