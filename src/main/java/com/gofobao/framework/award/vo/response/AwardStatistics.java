package com.gofobao.framework.award.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/13.
 */
@Data
public class AwardStatistics {
    @ApiModelProperty("体验金")
    private Double virtualMoney;
    @ApiModelProperty("优惠券个数")
    private Integer couponCount ;
    @ApiModelProperty("红包个数")
    private Integer redPackageCount;
}
