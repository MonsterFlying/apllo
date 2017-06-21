package com.gofobao.framework.system.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/21.
 */
@ApiModel
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoSysVersion {
    @ApiModelProperty("提示语")
    private String isEquls;
    @ApiModelProperty("是否最新")
    private Boolean isNew;
    @ApiModelProperty("下载链接")
    private String url;
    @ApiModelProperty("版本详情")
    private String details;
    @ApiModelProperty("是否强制更新 0:不需要 ;1：需要")
    private Integer force;
    @ApiModelProperty("版本号")
    private String viewVersion;
}

