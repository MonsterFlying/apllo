package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/15.
 */
@ApiModel
@Data
public class VoViewBuyProductPlanRes extends VoBaseResp {
    @ApiModelProperty("订单编号")
    private String orderNumber;
}
