package com.gofobao.framework.finance.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/31.
 */
@Data
@ApiModel
public class VoViewFinanceCollectionRes {

    @ApiModelProperty("回款 :应收本息(元); 还款： 应还本息")
    private String collectionMoney;

    @ApiModelProperty("回款：已收本息(元); 还款：已还本息")
    private String collectionMoneyYes="0";

    @ApiModelProperty("标名")
    private String financeName;

    @ApiModelProperty("总期数")
    private Integer timeLime;

    @ApiModelProperty("当前期数")
    private Integer order;

    @ApiModelProperty("期数id")
    private Long collectionId;

    @ApiModelProperty("当回款：0：未回款,1：已回款;   当还款：0：未还, 1：已还 ,2:还款复审中")
    private Integer status;
}
