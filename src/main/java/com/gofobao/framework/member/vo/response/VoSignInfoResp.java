package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@ApiModel
@Data
public class VoSignInfoResp extends VoBaseResp{
    @ApiModelProperty("自动投标签约状态")
    private boolean autoTenderState  ;

    @ApiModelProperty("自动债权转让状态")
    private boolean autoTransferState ;
}
