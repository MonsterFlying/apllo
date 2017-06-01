package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/31.
 */
@Data
public class VoCollectionOrderReq {

    private Long userId;

    @ApiModelProperty("时间")
    private String time;
}
