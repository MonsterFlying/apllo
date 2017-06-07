package com.gofobao.framework.award.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/7.
 */
@Data
public class CouponRes {
    @ApiModelProperty("流量大小")
    private String sizeStr;

    @ApiModelProperty("电话")
    private String phone;

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("有效期")
    private String expiryDate;

}
