package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/21.
 */
@ApiModel
@Data
public class VoProductCollect {
    @ApiModelProperty("商品id")
    private Long productItemId;
    @ApiModelProperty("是否上架")
    private Boolean isEnable;
    @ApiModelProperty("商品名")
    private String name;
    @ApiModelProperty("商品标题")
    private String title;
    @ApiModelProperty("图片路径")
    private String imgUrl;
    @ApiModelProperty("购买金额")
    private String lowest;
    @ApiModelProperty("sku集合")
    private List<VoSku> skuList;
}
