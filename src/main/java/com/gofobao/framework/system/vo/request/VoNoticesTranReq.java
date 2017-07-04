package com.gofobao.framework.system.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/3.
 */
@Data
public class VoNoticesTranReq {

    @ApiModelProperty(hidden = true)
    private Long userId;

    @ApiModelProperty("id集合")
    private List<Long> noticesIds;
}
