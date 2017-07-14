package com.gofobao.framework.lend.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/14.
 */
@Data
public class LendInfoList {


    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("金额")
    private String money;


    @ApiModelProperty("年利率")
    private String apr;


    @ApiModelProperty("期限(天)")
    private Integer timeLimit;


    @ApiModelProperty("应还时间")
    private String repayAt;


    @ApiModelProperty("实还时间")
    private String repayAtYes;


    @ApiModelProperty("借款Id")
    private Long repaymentId;

    @ApiModelProperty("用戶ID")
    private Long userId;


}
