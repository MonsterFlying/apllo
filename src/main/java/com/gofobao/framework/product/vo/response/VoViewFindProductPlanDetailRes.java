package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/14.
 */
@ApiModel
@Data
public class VoViewFindProductPlanDetailRes extends VoBaseResp {
    @ApiModelProperty("投资期限")
    private String timeLimit;
    @ApiModelProperty("起投金额")
    private String lower;
    @ApiModelProperty("年华利率")
    private String apr;
    @ApiModelProperty("还款方式")
    private String repayPattern;
    @ApiModelProperty("起息时间")
    private String startDate;
    @ApiModelProperty("手续费率")
    private String feeRatio;
    @ApiModelProperty("安全保障")
    private String safety;
}
