package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/14.
 */
@ApiModel
@Data
public class VoViewFindProductItemDetailsRes extends VoBaseResp {
    @ApiModelProperty("子商品详情列表")
    List<VoProductItemDetail> productItemDetailList;
}
