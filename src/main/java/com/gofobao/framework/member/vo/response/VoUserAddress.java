package com.gofobao.framework.member.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/8.
 */
@ApiModel
@Data
public class VoUserAddress {
    @ApiModelProperty("收货人")
    private String name;
    @ApiModelProperty("收货人电话")
    private String phone;
    @ApiModelProperty("所在省份")
    private String province;
    @ApiModelProperty("所在城市")
    private String city;
    @ApiModelProperty("详细")
    private String detailedAddress;
    @ApiModelProperty("是否是默认")
    private Boolean isDefault;
}
