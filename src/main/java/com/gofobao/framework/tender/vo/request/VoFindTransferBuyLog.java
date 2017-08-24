package com.gofobao.framework.tender.vo.request;

import com.gofobao.framework.common.page.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/8/22.
 */
@ApiModel
@Data
public class VoFindTransferBuyLog extends Page{
    @ApiModelProperty(hidden = true)
    private Long userId;
    @ApiModelProperty(name = "债权转让id，选填")
    private Long transferId;

}
