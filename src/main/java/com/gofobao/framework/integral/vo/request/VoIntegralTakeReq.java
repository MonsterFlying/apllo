package com.gofobao.framework.integral.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
public class VoIntegralTakeReq extends VoBaseReq{
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long userId;

    @NotNull
    @ApiModelProperty("待兑换积分")
    private Integer integer;

}
