package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by admin on 2017/8/10.
 */
@Data
public class VoSuggestAddReq {

    @ApiModelProperty(hidden = true)
    private Long userId;

    @NotNull(message = "意见不能为空")
    private String content;

}
