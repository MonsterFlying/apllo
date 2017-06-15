package com.gofobao.framework.borrow.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/15.
 */
@Data
@ApiModel("出借人")
public class Lender {

    @ApiModelProperty("用户名")
    private String name;

    @ApiModelProperty("出借金额")
    private String money;

    @ApiModelProperty("出借期限")
    private Integer timeLimit;

    @ApiModelProperty("月本息")
    private String monthAsReimbursement;
}
