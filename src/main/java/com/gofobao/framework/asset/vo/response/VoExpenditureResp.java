package com.gofobao.framework.asset.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/12/6.
 */
@ApiModel("累计支出详情")
@Data
public class VoExpenditureResp extends VoBaseResp {
    @ApiModelProperty("已支出总额")
    private String expandTotal;

    @ApiModelProperty("已还利息")
    private String expandInterest;

    @ApiModelProperty("已还利息管理费")
    private String expandInterestManager;

    @ApiModelProperty("账户管理费")
    private String expandAccountManager;

    @ApiModelProperty("逾期罚息")
    private String expandOverdueFee;

    @ApiModelProperty("费用")
    private String expandFee;

    @ApiModelProperty("其他支出")
    private String expandOther;

    @ApiModelProperty("待付利息")
    private String expandWaitInterest;

    @ApiModelProperty("待付利息管理费")
    private String expandWaitInterestManager;

    @ApiModelProperty("待付支出")
    private String expandWaitTotal;
}
