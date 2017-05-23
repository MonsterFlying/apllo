package com.gofobao.framework.currency.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/23.
 */
@ApiModel
@Data
public class VoCurrency {
    /**
     * 类型
     */
    @ApiModelProperty("类型")
    private String type;

    /**
     * 类型名称
     */
    @ApiModelProperty("类型名称")
    private String typeName;

    /**
     * 广福币数量
     */
    @ApiModelProperty("广福币数量")
    private String currency;

    /**
     * 可用广福币
     */
    @ApiModelProperty("可用广福币")
    private Integer totalCurrency;

    /**
     * 时间
     */
    @ApiModelProperty("时间")
    private String date;
}
