package com.gofobao.framework.member.vo.response.pc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/11.
 */
@Data
public class PaymentDetails {

    @ApiModelProperty("车贷待收本金")
    private String chedaiWaitCollectionPrincipal;
    @ApiModelProperty("车贷待收利息")
    private String chedaiWaitCollectionInterest;

    @ApiModelProperty("渠道待收本金")
    private String qudaoWaitCollectionPrincipal;
    @ApiModelProperty("渠道待收本金")
    private String qudaoWaitCollectionInterest;

    @ApiModelProperty("信用标待收本金")
    private String jingzhiWaitCollectionPrincipal;
    @ApiModelProperty("信用标待收本金")
    private String jingzhiWaitCollectionInterest;


}
