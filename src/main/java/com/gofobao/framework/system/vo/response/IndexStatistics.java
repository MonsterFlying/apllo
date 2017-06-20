package com.gofobao.framework.system.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/14.
 */
@Data
public class IndexStatistics {

    @ApiModelProperty("交易总额")
    private Long  transactionsTotal;

    @ApiModelProperty("待收总额")
    private Long dueTotal;

/*    @ApiModelProperty("安全运营天数")
    private String securityDays;*/

}
