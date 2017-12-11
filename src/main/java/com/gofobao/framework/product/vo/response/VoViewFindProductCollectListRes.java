package com.gofobao.framework.product.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/11/21.
 */
@ApiModel
@Data
public class VoViewFindProductCollectListRes extends VoBaseResp {
    @ApiModelProperty("商品收藏列表")
    private List<VoProductCollect> productCollectList;
}
