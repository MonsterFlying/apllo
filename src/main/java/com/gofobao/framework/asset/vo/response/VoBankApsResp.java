package com.gofobao.framework.asset.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/13 0013.
 */
@ApiModel
@Data
public class VoBankApsResp {
    @ApiModelProperty("银行地址")
    String addr ;
    @ApiModelProperty("银行名称")
    String bank ;
    @ApiModelProperty("银行所在城市")
    String city ;
    @ApiModelProperty("银行所在省")
    String province ;
    @ApiModelProperty("联行号")
    String bankCode ;
    @ApiModelProperty("支行名称")
    String lName ;
    @ApiModelProperty("支行电话")
    String  tel ;
}
