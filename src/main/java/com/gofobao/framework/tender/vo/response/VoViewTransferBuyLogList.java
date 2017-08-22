package com.gofobao.framework.tender.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.entity.TransferBuyLog;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Zeke on 2017/8/22.
 */
@ApiModel
@Data
public class VoViewTransferBuyLogList extends VoBaseResp {
    @ApiModelProperty("债权转让购买集合")
    private List<VoViewTransferBuyLog> voViewTransferBuyLogs;
}
