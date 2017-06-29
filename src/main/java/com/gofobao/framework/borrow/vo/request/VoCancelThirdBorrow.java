package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class VoCancelThirdBorrow {

    @ApiModelProperty(value = "用户id", dataType = "int")
    private Long userId;

    @ApiModelProperty(value = "标的id", dataType = "int")
    private String productId;
    /**
     * 募集日 YYYYMMDD
     */
    private String raiseDate;
    /**
     * 请求方保留 选填
     */
    private String acqRes;
}
