package com.gofobao.framework.repayment.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/5.
 */
@Data
public class VoOrdersList {

    @ApiModelProperty("时间")
    private String time;

    @ApiModelProperty("本息")
    private String collectionMoney;

    @ApiModelProperty("本息")
    private String principal;

    @ApiModelProperty("利息")
    private String interest;

    @ApiModelProperty("笔数")
    private Long orderCount;


}
