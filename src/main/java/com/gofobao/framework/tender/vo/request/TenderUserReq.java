package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/16.
 */
@ApiModel("")
@Data
public class TenderUserReq extends Page {
    private Long borrowId;
    @ApiModelProperty(hidden = true)
    private Long userId=0L;
}
