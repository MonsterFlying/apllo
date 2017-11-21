package com.gofobao.framework.product.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/15.
 */
@ApiModel
@Data
public class VoUserAddress {
    @ApiModelProperty("收货地址id")
    private String addressId;
    @ApiModelProperty("收件人姓名")
    private String name;
    @ApiModelProperty("收件人电话")
    private String phone;
    @ApiModelProperty("收货地址")
    private String address;
}
