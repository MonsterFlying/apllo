package com.gofobao.framework.repayment.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/8.
 */
@Data
public class LoanStatistics {

    @ApiModelProperty("编号")
    private String id;
    @ApiModelProperty("借款人")
    private String userName;
    @ApiModelProperty("应归还金额")
    private String collectionMoney;
    @ApiModelProperty("应归还日期")
    private String collectionAt;


}
