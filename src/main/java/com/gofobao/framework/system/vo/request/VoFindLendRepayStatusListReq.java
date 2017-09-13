package com.gofobao.framework.system.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/9/12.
 */
@ApiModel
@Data
public class VoFindLendRepayStatusListReq extends Page {
    @ApiModelProperty("借款id")
    private Long borrowId;
}
