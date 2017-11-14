package com.gofobao.framework.product.vo.request;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.product.vo.response.VoSku;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/13.
 */
@ApiModel
@Data
public class VoProductPlanDetail extends VoBaseResp {
    @ApiModelProperty("商品id")
    private Long productId;
}
