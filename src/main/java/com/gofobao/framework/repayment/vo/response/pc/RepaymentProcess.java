package com.gofobao.framework.repayment.vo.response.pc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/23.
 */
@ApiModel("还款流程时间")
@Data
public class RepaymentProcess {

    @ApiModelProperty("进度名")
    private String processName;

    @ApiModelProperty("时间")
    private String dateStr;

    @ApiModelProperty("状态")
    private Integer state;


/*
    @ApiModelProperty("还款时间")
    private String userSubmit;

    @ApiModelProperty("请求存管系统接收还款时间")
    private String sendJxSuccess;

    @ApiModelProperty("存管回调时间")
    private String notifyAt;

    @ApiModelProperty("最终还款时间")
    private String successAt;
*/

}
