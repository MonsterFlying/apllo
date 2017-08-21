package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/8/21.
 */
@Data
public class Event {
    @ApiModelProperty("促销类型: 1.红包, 2.积分, 3.现金券")
    private Integer marketingType;

    private String title;

    private String introduction;

    private String targerUrl;

    private String viewUrl;

    private String beginAt;

    private String endAt;
}
