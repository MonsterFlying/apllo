package com.gofobao.framework.tender.vo.response.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/6.
 */
@Data
public class VoViewTransferBuyWarpRes extends VoBaseResp {

    private List<TransferBuy> transferBuys = Lists.newArrayList();

    @ApiModelProperty("总ji记录数")
    private Integer totalCount = 0;
}
