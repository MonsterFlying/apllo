package com.gofobao.framework.asset.vo.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/19.
 */
@Data
@ApiModel
public class VoUserAssetInfoReq extends VoBaseReq{
    @ApiModelProperty(hidden = true)
    @JsonIgnore
    private Long userId;
}
