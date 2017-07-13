package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/12.
 */
@Data
@ApiModel("转让中")
public class TransferOf {
    @ApiModelProperty("标名")
    private String name;
    @ApiModelProperty("时间")
    private String createTime;
    @ApiModelProperty("转让本金")
    private String principal;
    @ApiModelProperty("年华利率")
    private String apr;
    @ApiModelProperty("进度")
    private String spend;
    @ApiModelProperty("标ID")
    private Long borrowId;
    @ApiModelProperty("是否可以取消债权转让")
    private Boolean cancel;

}
