package com.gofobao.framework.integral.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
@ApiModel
public class VoIntegral {
    /**
     * 积分记录ID
     */
    @ApiModelProperty(value = "积分记录id")
    private Long id;
    /**
     * 积分
     */
    @ApiModelProperty(value = "积分")
    private String integral;
    /**
     * 积分类型
     */
    @ApiModelProperty(value = "积分类型")
    private String type;

    /**
     * 积分类型名称
     */
    @ApiModelProperty(value = "积分类型名称")
    private String typeName;

    /**
     * 总积分
     */
    @ApiModelProperty(value = "总积分")
    private Long totalIntegral;


    /**
     * 已用积分
     */
    @ApiModelProperty(value = "已用积分")
    private Long usedIntegral;


    /**
     * 可用积分
     */
    @ApiModelProperty(value = "可用积分")
    private Long useIntegral;


    /**
     * 时间
     */
    @ApiModelProperty(value = "时间")
    private String time;
}
