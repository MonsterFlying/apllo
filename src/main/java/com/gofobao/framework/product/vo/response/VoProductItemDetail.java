package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Zeke on 2017/11/13.
 */
@ApiModel
@Data
public class VoProductItemDetail {
    @ApiModelProperty("子商品id")
    private Long productItemId;
    @ApiModelProperty("商品名")
    private String name;
    @ApiModelProperty("图片url")
    private String imgUrl;
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
    @ApiModelProperty("商品详情")
    private String details;
    @ApiModelProperty("售后服务")
    private String afterSalesService;
    @ApiModelProperty("Q&A 问答")
    private String qAndA;
}
