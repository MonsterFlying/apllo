package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/20.
 */
@ApiModel
@Data
public class VoRegisterOfficialBorrow {
    @ApiModelProperty("借款id")
    private Long borrowId;
}
