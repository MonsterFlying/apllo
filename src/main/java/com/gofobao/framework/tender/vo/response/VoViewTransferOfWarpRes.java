package com.gofobao.framework.tender.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/6/12.
 */
@Data
public class VoViewTransferOfWarpRes extends VoBaseResp{
    private List<TransferOf>transferOfs= Lists.newArrayList();

    @ApiModelProperty("总记录数")
    private Integer totalCount=0;
}
