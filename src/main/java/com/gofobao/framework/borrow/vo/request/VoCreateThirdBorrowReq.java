package com.gofobao.framework.borrow.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/6/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel
public class VoCreateThirdBorrowReq extends VoBaseReq{

    @ApiModelProperty(value = "用户id", dataType = "int")
    private Long userId;

    @ApiModelProperty(value = "标的id", dataType = "String")
    private String productId;

    @ApiModelProperty(value = "标的描述", dataType = "String")
    private String productDesc;

    @ApiModelProperty(value = "募集日 YYYYMMDD", dataType = "String")
    private String raiseDate;

    @ApiModelProperty(value = "募集结束日期  YYYYMMDD 募集期不得超过规定工作日，超过结束日期未能满标，标的失效，不能再投标或满标，但需要P2P主动发起撤销所有", dataType = "String")
    private String raiseEndDate;

    @ApiModelProperty(value = "付息方式 0-到期与本金一起归还 1-每月固定日期支付   2-每月不确定日期支付", dataType = "String")
    private String intType;

    @ApiModelProperty(value = "利息每月支付日 DD 选填 付息方式为1时必填；若设置日期大于月份最后一天时，则为该月最后一天支付 平台仅记录", dataType = "String")
    private String intPayDay;

    @ApiModelProperty(value = "借款期限 天数，从满标日期开始计算", dataType = "String")
    private String duration;

    @ApiModelProperty(value = "交易金额 12,2 借款金额", dataType = "String")
    private String txAmount;

    @ApiModelProperty(value = "年化利率 8,5 百分数", dataType = "String")
    private String rate;

    @ApiModelProperty(value = "平台手续费  12,2 平台收取的总手续费 选填", dataType = "String")
    private String txFee;

    @ApiModelProperty(value = "担保人用户id", dataType = "int")
    private Long bailUserId;

    @ApiModelProperty(value = "名义借款人id", dataType = "int")
    private Long nominalUserId;

    @ApiModelProperty(value = "请求方保留 选填", dataType = "String")
    @NotNull(message = "请求方保留参数不能为空")
    private String acqRes;
}
