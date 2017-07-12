package com.gofobao.framework.member.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/12.
 */
@Data
public class ExpenditureDetail extends VoBaseResp {

    @ApiModelProperty("已支出总额")
    private String  ExpenditureTotal;

    @ApiModelProperty("已还利息")
    private String  paymentInterest;

    @ApiModelProperty("已付利息管理费")
    private String  interestManageFee;

    @ApiModelProperty("账户管理费")
    private String  accountMangeFee;

    @ApiModelProperty("逾期罚息")
    private String  overdueFee;

    @ApiModelProperty("费用")
    private String  fee;

    @ApiModelProperty("其他支出")
    private String  otherFee;

    @ApiModelProperty("待付支出")
    private String  waitExpendTotal;

    @ApiModelProperty("待付利息")
    private String  waitExpendInterest;

    @ApiModelProperty("待付利息管理费")
    private String  waitExpendInterestManageFee;


}
