package com.gofobao.framework.repayment.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/5.
 */
@Data
public class VoOrderListReq extends Page {
    @ApiModelProperty(hidden = true)
    private Long userId;
}
