package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/13.
 */
@ApiModel
@Data
public class VoProductPlanDetail {
    @ApiModelProperty("商品名")
    private String name;
    @ApiModelProperty("子标题")
    private String title;
    @ApiModelProperty("库存")
    private String inventory;
    @ApiModelProperty("标识价格")
    private String price;
    @ApiModelProperty("折扣价格")
    private String discountPrice;
    @ApiModelProperty("sku集合")
    private List<VoSku> skuList;
}
