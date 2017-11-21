package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/17.
 */
@ApiModel
@Data
public class VoProductOrder {
    @ApiModelProperty("商品计划类型：0广富送 1.联通送")
    private Integer type;
    @ApiModelProperty("订单状态：状态：1.未付款 2.已付款 3.待发货 4.待收货 5.已完成 6.已取消")
    private Integer status;
    @ApiModelProperty("订单商品集合")
    private List<VoProductItem> productItemList;
    @ApiModelProperty("商品件数")
    private Integer number;
    @ApiModelProperty("支付金额")
    private String payMoney;
    @ApiModelProperty("订单编号")
    private String orderNumber;
}
