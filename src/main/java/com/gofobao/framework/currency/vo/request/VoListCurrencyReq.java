package com.gofobao.framework.currency.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/23.
 */
@ApiModel
@Data
public class VoListCurrencyReq extends Page{
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long userId;
}
