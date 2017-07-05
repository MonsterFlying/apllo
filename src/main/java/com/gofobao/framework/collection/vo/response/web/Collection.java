package com.gofobao.framework.collection.vo.response.web;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/5.
 */
@Data
public class Collection {

    @ApiModelProperty("标名")
    private String borrowName;

    @ApiModelProperty("总期数")
    private Integer timeLimit;


    @ApiModelProperty("当期")
    private Integer order;

    @ApiModelProperty("待收本息")
    private String principal;


    @ApiModelProperty("待收利息")
    private String interest;


    @ApiModelProperty("标名")
    private String earnings;

    @ApiModelProperty("还款时间")
    private String collectionAt;

}
