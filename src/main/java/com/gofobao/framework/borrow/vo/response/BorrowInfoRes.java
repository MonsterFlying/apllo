package com.gofobao.framework.borrow.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;

/**
 * Created by admin on 2017/6/9.
 */
@Data
public class   BorrowInfoRes {

    @ApiModelProperty("每万元收益")
    private String earnings;

    @ApiModelProperty("期限")
    private String timeLimit;

    @ApiModelProperty("还款方式；0：按月分期；1：一次性还本付息；2：先息后本")
    private Integer repayFashion;

    @ApiModelProperty("投标记录")
    private String tenderCount;

    @ApiModelProperty("起投金额")
    private String lowest;

    @ApiModelProperty("融资金额")
    private String money;

    @ApiModelProperty("剩余金额")
    private String moneyYes;

    @ApiModelProperty("进度")
    private String spend;

    @ApiModelProperty("年华率")
    private String apr;

    @ApiModelProperty("结束时间")
    private String endAt;

    @ApiModelProperty("满标时间")
    private String successAt;

}