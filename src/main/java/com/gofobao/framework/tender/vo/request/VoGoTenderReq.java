package com.gofobao.framework.tender.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by Max on 17/3/30.
 */
@ApiModel("马上转让")
@Data
public class VoGoTenderReq {
    @ApiModelProperty(hidden = true)
    private Long userId;

    @ApiModelProperty("投资记录Id")
    @NotNull(message = "投机记录Id不能为空")
    @Min(value = 0, message = "投资记录Id，不能小于0")
    private Long tenderId;

}
