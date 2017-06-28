package com.gofobao.framework.borrow.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/5/19.
 */
@Data
@ApiModel
public class VoBorrowTenderUserRes {
    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("投标时间")
    private String date;

    @ApiModelProperty("投标类型")
    public String type;
    @ApiModelProperty("投标金额")
    public String money;
    // TODO 修改投标记录
    @ApiModelProperty("投标有效金额")
    private String validMoney;

}
