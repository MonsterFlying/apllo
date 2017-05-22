package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 17/5/22.
 */
@Data
@ApiModel
public class VoBankResp extends VoBaseReq {
    @ApiModelProperty(value = "银行卡ID")
    private Long id ;

    @ApiModelProperty(value = "银行卡号")
    private String bankNo ;
}
