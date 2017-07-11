package com.gofobao.framework.collection.vo.response.web;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/5.
 */
@Data
public class CollectionList {

    @ApiModelProperty("时间")
    private String createTime;

    @ApiModelProperty("待收本息")
    private String collectionMoney;

    @ApiModelProperty("待收本金")
    private String principal;

    @ApiModelProperty("待收利息")
    private String interest;

    @ApiModelProperty("笔数")
    private Long orderCount;

    @ApiModelProperty("回款")
    private Long collectionId;
}
