package com.gofobao.framework.comment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 积分记录
 */
@Data
@ApiModel
public class VoTopicsIntegralRecord {
    @ApiModelProperty("操作名称")
    private String opName = "";

    @ApiModelProperty("操作数量")
    private Long opValue = 0L;

    @ApiModelProperty("操作符号, D: 加, C: -")
    private String opFlag = "C";

    @ApiModelProperty("可用积分")
    private Long useIntegral = 0L;

    @ApiModelProperty("累计积分")
    private Long totalUseIntegral = 0L;

    @ApiModelProperty("操作时间")
    private Date createDate  ;
}
