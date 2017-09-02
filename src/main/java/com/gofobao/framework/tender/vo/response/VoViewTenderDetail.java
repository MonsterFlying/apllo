package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/2.
 */
@Data
@ApiModel("投资详情")
public class VoViewTenderDetail {

    @ApiModelProperty("项目名")
    private String borrowName;

    @ApiModelProperty("投资金额")
    private String money;

    @ApiModelProperty("投资时间")
    private String createdAt;

    @ApiModelProperty("年华利率")
    private String apr;

    @ApiModelProperty("期限")
    private String timeLimit;

    @ApiModelProperty("还款方式")
    private String repayFashion;

    @ApiModelProperty("起息时间")
    private String successAt="";

    @ApiModelProperty("状态描述")
    private String statusStr;

    @ApiModelProperty("状态:1:投标中； 2:还款中 ;3:已结清")
    private Integer status;

    @ApiModelProperty("预期收益")
    private String receivableInterest;

    @ApiModelProperty("已收利息")
    private String interest="0";

    @ApiModelProperty("已收本金")
    private String principal="0";

    @ApiModelProperty("标ID")
    private Long borrowId;

    @ApiModelProperty("投标ID")
    private Long tenderId;

    @ApiModelProperty("是否是摘转")
    private Boolean isTransfer;

}
