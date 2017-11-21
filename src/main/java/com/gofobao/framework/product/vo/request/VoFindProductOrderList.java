package com.gofobao.framework.product.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/17.
 */
@ApiModel
@Data
public class VoFindProductOrderList extends Page {
    @ApiModelProperty("搜索关键词 (商品名称/订单号) 非必填")
    private String searchStr;
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;
    @ApiModelProperty("状态：0全部 1.待付款 2.待收货 3.已完成 4.已取消 默认0")
    private Integer status = 0;
}
