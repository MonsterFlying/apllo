package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/15.
 */
@ApiModel
@Data
public class VoViewBoughtProductPlanRes extends VoBaseResp {
    @ApiModelProperty("子商品id")
    private String productItemId;
    @ApiModelProperty("计划id")
    private String planId;
    @ApiModelProperty("商品名")
    private String name;
    @ApiModelProperty("小标题")
    private String title;
    @ApiModelProperty("商品图片")
    private String imgUrl;
    @ApiModelProperty("商品sku")
    private List<VoSku> skuList;
    @ApiModelProperty("广富送计划名称")
    private String planName;
    @ApiModelProperty("显示购买金额")
    private String showLowest;
    @ApiModelProperty("购买金额")
    private String lowest;
    @ApiModelProperty("额外收益")
    private String earnings;
    @ApiModelProperty("显示可用余额")
    private String showUseMoney;
    @ApiModelProperty("可用余额")
    private String useMoney;
    @ApiModelProperty("是否有收货地址 true存在 false不存在")
    private Boolean existAddress;
    @ApiModelProperty("收货地址")
    private VoUserAddress userAddress;
}
