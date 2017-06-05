package com.gofobao.framework.repayment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/5.
 */

@ApiModel("借款详情列表")
@Data
public class VoViewLoanList {

    @ApiModelProperty("总期数")
    private Integer orderCount;

    @ApiModelProperty("应还本息")
    private String sumRepayMoney;

    @ApiModelProperty("借款列表")
    private List<VoLoanInfo> voLoanInfoList;

}
