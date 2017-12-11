package com.gofobao.framework.product.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/11/10.
 */
@Data
@ApiModel
public class VoFindProductPlanDetail {
    @ApiModelProperty("子商品id")
    @NotNull(message = "商品id不能为空")
    private Long productItemId;
}
