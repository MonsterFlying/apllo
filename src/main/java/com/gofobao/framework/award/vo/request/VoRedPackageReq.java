package com.gofobao.framework.award.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/7.
 */
@Data
@ApiModel("红包列表")
public class VoRedPackageReq extends Page {
    @ApiModelProperty(hidden = true)
    private Long userId;
    @ApiModelProperty("红包状态 0：未领取；1：已领取；2：已过期")
    private Integer status;
}
