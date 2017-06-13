package com.gofobao.framework.award.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/13.
 */
@Data
public class VoVirtualReq {

    @ApiModelProperty(hidden = true)
    private Long userId;

    private Long id;
}
