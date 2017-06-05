package com.gofobao.framework.repayment.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * Created by admin on 2017/6/2.
 */
@Data
public class VoLoanListReq extends Page {

    @ApiModelProperty(hidden = true)
    private  Long userId;

    @ApiModelProperty(hidden = true)
    private Integer type;

    @ApiModelProperty(hidden = true)
    private Integer status;

}
