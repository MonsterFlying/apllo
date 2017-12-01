package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class VoAccountStatusResp  extends VoBaseResp{
    @ApiModelProperty("交易密码状态")
    private boolean passwordState ;

    @ApiModelProperty("平台投标授权")
    private boolean tenderState ;

    @ApiModelProperty("债权转让授权")
    private boolean transferState ;
}
