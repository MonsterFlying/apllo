package com.gofobao.framework.award.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/8.
 */
@Data
@ApiModel("开启红包")
public class VoOpenRedPackageReq {

    @ApiModelProperty("红包id")
    private Integer redPackageId;

    @ApiModelProperty(hidden = true)
    private Long userId;

}
