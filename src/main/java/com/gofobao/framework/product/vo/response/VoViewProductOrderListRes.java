package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/17.
 */
@ApiModel
@Data
public class VoViewProductOrderListRes extends VoBaseResp {
    @ApiModelProperty("商品订单集合")
    private List<VoProductOrder> productOrderList;
}
