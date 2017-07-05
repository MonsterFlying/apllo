package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/4.
 */
@Data
public class PcAutoTender {

    @ApiModelProperty("排名")
    private Long id;
    @ApiModelProperty("投标额度")
    private String way;

    @ApiModelProperty("最小投标额")
    private String lowest;

    @ApiModelProperty("期限范围")
    private String timelimitType;

    @ApiModelProperty("利率范围")
    private String scope;

    @ApiModelProperty("标类型")
    private String borrowTypes;

    @ApiModelProperty("排队天数")
    private Long queueDays;

    @ApiModelProperty("是否开启")
    private Boolean isOpen;

}
