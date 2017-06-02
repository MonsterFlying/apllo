package com.gofobao.framework.collection.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/31.
 */
@Data
public class VoViewCollectionOrderRes {

    @ApiModelProperty("待收本息(元)")
    private String collectionMoney;

    @ApiModelProperty("已收本息(元)")
    private String collectionMoneyYes;

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("总期数")
    private Integer timeLime;

    @ApiModelProperty("当前期数")
    private Integer order;


}
