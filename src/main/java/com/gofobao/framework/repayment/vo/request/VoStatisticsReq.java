package com.gofobao.framework.repayment.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/8.
 */
@Data
public class VoStatisticsReq extends Page {

    @ApiModelProperty("一周：gt7days,30天内有逾期未还：lt30days,30天以上有逾期未还：gt30days,逾期已归还：lateRepay")
    private String type;

    @ApiModelProperty(hidden = true)
    private Long userId;
}
