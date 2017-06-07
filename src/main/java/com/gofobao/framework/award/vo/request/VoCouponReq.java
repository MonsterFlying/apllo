package com.gofobao.framework.award.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/7.
 */
@Data

public class VoCouponReq extends Page {
    @ApiModelProperty("状态 0:无效； 1:有效")
    private Integer status;
    @ApiModelProperty("用户id")
    private Long userId;

}
