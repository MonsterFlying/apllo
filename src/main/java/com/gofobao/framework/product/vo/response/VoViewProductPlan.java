package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/10.
 */
@ApiModel
@Data
public class VoViewProductPlan {
    @ApiModelProperty("商品名")
    private String name;
    @ApiModelProperty("小标题")
    private String title;
    @ApiModelProperty("展示图片")
    private String imgUrl;
    @ApiModelProperty("展示价格")
    private String showPrice;
}
