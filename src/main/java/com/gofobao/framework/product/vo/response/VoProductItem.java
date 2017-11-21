package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/20.
 */
@Data
@ApiModel
public class VoProductItem {
    @ApiModelProperty("图片url")
    private String imgUrl;
    @ApiModelProperty("商品名")
    private String productName;
    @ApiModelProperty("商品小标题")
    private String productTitle;
    @ApiModelProperty("商品sku")
    private List<VoSku> skuList;
}
