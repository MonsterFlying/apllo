package com.gofobao.framework.asset.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/3.
 */
@Data
public class VoPcCashLogs extends Page {
    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("提现状态（0待处理；4处理中；1已成功；2已失败；3已取消）")
    private Integer status;

}
