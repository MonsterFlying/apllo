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

    @ApiModelProperty("-1.取消提现.0:申请中,1.系统审核通过,2.系统审核不通过, 3.银行提现成功.4.银行提现失败")
    private Integer status;

}
