package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/13.
 */
@ApiModel
@Data
public class VoSku {
    @ApiModelProperty("classNo")
    private String classNo;
    @ApiModelProperty("sku名")
    private String name;
    @ApiModelProperty("序号")
    private String no;
    @ApiModelProperty("分类名")
    private String className;
}
