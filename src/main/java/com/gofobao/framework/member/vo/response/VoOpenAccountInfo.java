package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Max on 2017/5/17.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("存管信息")
public class VoOpenAccountInfo extends VoBaseResp {
    @ApiModelProperty("签约状态")
    private boolean signedState;
    @ApiModelProperty("开户名称")
    private String realName;

    @ApiModelProperty("存管行")
    private String openAccountOfficeName = "江西银行总行营业部";

    @ApiModelProperty("存管账户")
    private String accountId;

    @ApiModelProperty("存管手机号")
    private String phone;

    @ApiModelProperty("银行卡logo")
    private String bankLogo;

    @ApiModelProperty("银行卡名称")
    private String bankName;

    @ApiModelProperty("银行卡号")
    private String bankNo;
    @ApiModelProperty("银行卡密码状态")
    private boolean passwordState;

    @ApiModelProperty("关于存管")
    private String about = "江西银行总行营业部";

}
