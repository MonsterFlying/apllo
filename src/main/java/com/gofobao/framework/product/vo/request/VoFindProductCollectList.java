package com.gofobao.framework.product.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/11/21.
 */
@ApiModel
@Data
public class VoFindProductCollectList extends Page {
    /**
     * 会员Id
     */
    @ApiModelProperty(name = "userId", hidden = true)
    private Long userId;
}
