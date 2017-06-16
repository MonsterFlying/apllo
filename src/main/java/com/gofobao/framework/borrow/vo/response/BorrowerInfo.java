package com.gofobao.framework.borrow.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/15.
 */
@Data
@ApiModel("借款人")
public class BorrowerInfo {
    @ApiModelProperty("用户名")
    private String name;
    @ApiModelProperty("身份证")
    private String idCard;

    @ApiModelProperty("标ID")
    private Long borrowId;
}
