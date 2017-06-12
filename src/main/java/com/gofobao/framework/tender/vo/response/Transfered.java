package com.gofobao.framework.tender.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/12.
 */
@Data
public class Transfered {
    @ApiModelProperty("标名")
    private String name;

    @ApiModelProperty("转让本金")
    private String principal;

    @ApiModelProperty("转让日期")
    private String time;

    @ApiModelProperty("转让费用")
    private String cost;

}
