package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/9/7.
 */
@Data
@ApiModel
public class VoSendAgainVerify {
    @ApiModelProperty("借款id")
    private Long borrowId;
}
