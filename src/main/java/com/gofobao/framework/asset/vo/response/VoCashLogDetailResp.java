package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/15 0015.
 */
@ApiModel
@Data
public class VoCashLogDetailResp  extends VoBaseResp{
    @ApiModelProperty("提现金额")
    private String cashMoney ;

    @ApiModelProperty("手续费")
    private String fee ;

    @ApiModelProperty("到账金额")
    private String realCashMoney ;

    @ApiModelProperty("提现银行")
    private String bankNameAndCardNo;

    @ApiModelProperty("提现时间")
    private String cashTime ;

    @ApiModelProperty("银行处理时间")
    private String bankProcessTime ;

    @ApiModelProperty("提现状态描述")
    private String statusMsg ;

    @ApiModelProperty("提现状态:  // -1.取消提现.0:申请中,1.系统审核通过, 2.系统审核不通过,  3.银行提现成功. 4.银行提现失败.")
    private Integer status ;

    @ApiModelProperty("真实到账时间")
    private String realCashTime ;
}
