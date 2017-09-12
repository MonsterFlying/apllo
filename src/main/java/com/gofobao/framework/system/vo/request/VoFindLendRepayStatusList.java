package com.gofobao.framework.system.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/9/12.
 */
@ApiModel
@Data
public class VoFindLendRepayStatusList {
    @ApiModelProperty("借款id")
    private Long borrowId;
}
