package com.gofobao.framework.tender.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 转让借款视图类
 * Created by Max on 2017/3/14.
 */
@ApiModel("转让借款视图类")
@Data
public class VoTransferTenderReq {
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;
    @ApiModelProperty(name = "tenderId", value = "投标id", dataType = "int", required = true)
    @NotNull(message = "投标id不能为空!")
    private Long tenderId;
    @ApiModelProperty("是否是部分转让")
    private Boolean isAll = true;
    @ApiModelProperty("部分转让回款id集合 用英文,分隔")
    private String borrowCollectionIds;
}
