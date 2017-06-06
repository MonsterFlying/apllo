package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 17/6/6.
 */
@Data
@ApiModel
public class VoOpenAccountResp extends VoBaseResp {
    @ApiModelProperty("开户银行名称")
    private String openAccountBankName;
    @ApiModelProperty("开户卡号")
    private String account;
    @ApiModelProperty("开户名称")
    private String name;

}
