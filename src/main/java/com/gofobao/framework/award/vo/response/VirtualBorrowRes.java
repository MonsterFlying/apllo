package com.gofobao.framework.award.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/9.
 */
@Data
public class VirtualBorrowRes {

    @ApiModelProperty("标名")
    private String name;

    @ApiModelProperty("期限")
    private Integer timeLimit;

    @ApiModelProperty("年华利率")
    private String apr;

    @ApiModelProperty("还款方式")
    private String repayFashion;

    @ApiModelProperty("投标金额")
    private String money;

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("时间")
    private String time;
}
