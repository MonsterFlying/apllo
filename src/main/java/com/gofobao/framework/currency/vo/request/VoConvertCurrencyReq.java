package com.gofobao.framework.currency.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/5/23.
 */
@ApiModel
@Data
public class VoConvertCurrencyReq extends VoBaseReq{
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long userId;

    @ApiModelProperty(name = "currency", value = "兑换广福币数量", dataType = "int", required = true)
    @NotNull(message = "兑换广福币数量不能为空")
    @Min(value = 1, message = "数量不能为0")
    private Integer currency;
}
