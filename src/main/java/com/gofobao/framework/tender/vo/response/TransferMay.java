package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/12.
 */
@ApiModel("可转让")
@Data
public class TransferMay {
    @ApiModelProperty("标名")
    private String name;
    @ApiModelProperty("剩余期数")
    private Integer order;
    @ApiModelProperty("可转让金额")
    private String principal;
    @ApiModelProperty("待收本息")
    private String interest;
    @ApiModelProperty("下个还款日")
    private String nextCollectionAt;
    @ApiModelProperty("投标ID")
    private Long tenderId;


}
