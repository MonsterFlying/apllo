package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/29.
 */
@Data
public class VoDueInRes extends VoBaseResp {

    @ApiModelProperty("待收利息")
    private String dueInInterest;

    @ApiModelProperty("待收本金")
    private String duePrincipal;

    @ApiModelProperty("总待收")
    private String dueInTotal;
}
