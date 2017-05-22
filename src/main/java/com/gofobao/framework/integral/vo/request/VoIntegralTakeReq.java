package com.gofobao.framework.integral.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/5/22.
 */
public class VoIntegralTakeReq extends VoBaseReq{
    @NotNull
    @ApiModelProperty("待兑换积分")
    private Integer integer;

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }
}
