package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/1.
 */

@Data

public class VoFinanceInvestListReq extends Page {

    @ApiModelProperty(hidden = true)
    private Long userId;


    @ApiModelProperty(hidden = true)
    private Integer type;

}
