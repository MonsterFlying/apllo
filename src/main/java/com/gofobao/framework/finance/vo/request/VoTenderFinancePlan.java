package com.gofobao.framework.finance.vo.request;

import com.gofobao.framework.common.qiniu.util.StringUtils;
import com.gofobao.framework.helper.MoneyHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/8/14.
 */
@ApiModel
@Data
public class VoTenderFinancePlan {
    @ApiModelProperty(hidden = true)
    private Long userId;
    @ApiModelProperty("理财计划id")
    private Long financePlanId;
    @ApiModelProperty("购买理财计划金额")
    private Long money;
    @ApiModelProperty
    private String remark;
    @ApiModelProperty(hidden = true)
    private String requestSource = "0";

    public String getRequestSource() {
        if (StringUtils.isNullOrEmpty(this.requestSource)) {
            return "0";
        } else {
            return this.requestSource;
        }
    }

    public void setRequestSource(String requestSource) {
        if (StringUtils.isNullOrEmpty(requestSource)) {
            this.requestSource = "0";
        } else {
            try {
                Long.parseLong(requestSource);
            } catch (Exception e) {
                requestSource = "0";
            }
            this.requestSource = requestSource;
        }
    }
}
