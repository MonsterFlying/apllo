package com.gofobao.framework.tender.vo.response.web;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/4.
 */
@Data
public class PcAutoTender {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("排名")
    private Integer order;

    @ApiModelProperty("投标额度 ：0、余额； 1、固定金额")
    private Integer amount;

    @ApiModelProperty("最小投标额")
    private String lowest;

    @ApiModelProperty("期限类型（0、不限定，1、按月，2、按天）")
    private Integer timeLimitType;

    @ApiModelProperty("利率范围")
    private String scope;

    @ApiModelProperty("投标种类（0：车贷标；4、渠道标；1、信用标；3、转让标）")
    private String borrowTypes;

    @ApiModelProperty("排队天数")
    private Integer queueDays;

    @ApiModelProperty("是否开启")
    private Boolean isOpen;

}
