package com.gofobao.framework.repayment.vo.response.pc;

import com.gofobao.framework.system.vo.response.VoFindRepayStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/6.
 */
@Data
public class VoCollection {
    @ApiModelProperty("还款id")
    private Long repaymentId;

    @ApiModelProperty("标m名")
    private String borrowName;

    @ApiModelProperty("总期数")
    private Integer timeLimit;

    @ApiModelProperty("当期")
    private Integer order;

    @ApiModelProperty("本金")
    private String principal;

    @ApiModelProperty("利息")
    private String interest;

    @ApiModelProperty("还款时间")
    private String repayAt;

    @ApiModelProperty("标是否是摘草")
    private boolean isLend;

    @ApiModelProperty("状态 0:未还款;1 以还 ；2：待复审")
    private Integer status;

    @ApiModelProperty("还款状态")
    private List<VoFindRepayStatus> voFindRepayStatusList;

    @ApiModelProperty("还款滞纳金")
    private String lateInterest;
}
