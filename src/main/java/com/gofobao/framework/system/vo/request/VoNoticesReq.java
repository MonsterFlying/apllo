package com.gofobao.framework.system.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/15.
 */
@Data
public class VoNoticesReq extends Page {
    @ApiModelProperty(hidden = true)
    private Long userId;

    private Long id;

    @ApiModelProperty(hidden = true)
    private Integer type;
}
