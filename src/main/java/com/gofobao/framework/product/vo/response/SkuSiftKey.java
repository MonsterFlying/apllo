package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/29.
 */
@ApiModel
@Data
public class SkuSiftKey {
    @ApiModelProperty("sku分类名")
    private String skuClassName;
    @ApiModelProperty("sku分类id")
    private String skuClassId;
    @ApiModelProperty("sku集合")
    private List<Sku> skuList;

    @ApiModel
    @Data
    public class Sku {
        @ApiModelProperty("sku名")
        private String skuName;
        @ApiModelProperty("skuId")
        private String skuId;
    }
}
