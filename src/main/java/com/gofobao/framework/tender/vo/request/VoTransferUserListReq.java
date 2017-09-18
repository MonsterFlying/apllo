package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/8/9.
 */
@Data
public class VoTransferUserListReq extends Page {

    private Long transferId;

    @ApiModelProperty(hidden = true)
    private Long userId=0L;
}
