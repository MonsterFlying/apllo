package com.gofobao.framework.finance.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/8/14.
 */
@Data
public class PlanBuyer {

    private String userName;

    @ApiModelProperty("'来源；0：PC；1：ANDROID；2：IOS；3：H5'")
    private Integer  source;

    private String validMoney;

    private String date;
}
