package com.gofobao.framework.collection.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/31.
 */
@Data
@ApiModel
public class VoViewCollectionOrderRes {

    @ApiModelProperty("回款 :应收本息(元); 还款： 应还本息")
    private String collectionMoney;

    @ApiModelProperty("回款：已收本息(元); 还款：已还本息")
    private String collectionMoneyYes="0";

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("总期数")
    private Integer timeLime;

    @ApiModelProperty("当前期数")
    private Integer order;

    @ApiModelProperty("期数id")
    private Long collectionId;

}
