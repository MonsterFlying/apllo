package com.gofobao.framework.collection.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/5.
 */
@Data
public class OrderListReq extends Page {
    @ApiModelProperty(hidden = true)
    private Long userId;
}
