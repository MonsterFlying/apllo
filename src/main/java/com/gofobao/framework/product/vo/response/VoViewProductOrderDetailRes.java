package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/13.
 */
@ApiModel
@Data
public class VoViewProductOrderDetailRes extends VoBaseResp {
    @ApiModelProperty("1.等待买家付款 2.买家已付款 3.等待卖家发货 4.等待买家收货 5.交易成功 6.交易取消")
    private Integer status;
    @ApiModelProperty("物流名称")
    private String expressName;
    @ApiModelProperty("物流编号")
    private String expressNumber;
    @ApiModelProperty("收货人")
    private String logisticsName;
    @ApiModelProperty("收货地址")
    private String logisticsAddress;
    @ApiModelProperty("联系电话")
    private String logisticsPhone;
    @ApiModelProperty("商品记录集合")
    private List<VoProductItem> productItemList;
    @ApiModelProperty("购买金额")
    private String buyMoney;
    @ApiModelProperty("手续费")
    private String fee;
    @ApiModelProperty("付款金额")
    private String payMoney;
    @ApiModelProperty("额外收益")
    private String earnings;
    @ApiModelProperty("订单编号")
    private String orderNumber;
    @ApiModelProperty("交易流水号")
    private String payNumber;
    @ApiModelProperty("订单创建时间")
    private String createAt;
    @ApiModelProperty("订单支付时间")
    private String payAt;
}
