package com.gofobao.framework.asset.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/13 0013.
 */
@ApiModel
@Data
public class VoCashLogResp {
    @ApiModelProperty("银行名称和卡号")
    private String bankNameAndCardNo ;
    @ApiModelProperty("银行名称和卡号")
    private String cashMoney ;
    @ApiModelProperty("银行名称和卡号")
    private String createTime ;
    @ApiModelProperty("银行名称和卡号")
    private String msg ;
    @ApiModelProperty("提现状态(0.提现申请中,1.提现成功. 2.提现失败)")
    private Integer state ;
    @ApiModelProperty("提现记录ID")
    private Long id;
}
