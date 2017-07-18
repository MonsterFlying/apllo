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
@ApiModel("用户基础信息")
public class VoBasicUserInfoResp extends VoBaseResp{
    @ApiModelProperty("头像链接")
    private String avatarUrl ;

    @ApiModelProperty("vip状态")
    private boolean vipState ;

    @ApiModelProperty("用户名（已经加密）")
    private String username ;

    @ApiModelProperty("手机（已经加密）")
    private String phone;

    @ApiModelProperty("邮箱（已经加密）")
    private String email ;

    @ApiModelProperty("实名（已经加密）")
    private String realname ;

    @ApiModelProperty("银行卡账号（已经加密）")
    private String bankAccout ;

    @ApiModelProperty("身份证号（已经加密）")
    private String idNo ;

    @ApiModelProperty("身份证号状态")
    private boolean idNoState ;

    @ApiModelProperty("银行存管状态")
    private boolean thirdAccountState;

    @ApiModelProperty("用户名状态")
    private boolean usernameState;

    @ApiModelProperty("是否绑定手机状态")
    private boolean phoneState;

    @ApiModelProperty("是否绑定手机状态")
    private boolean emailState ;

    @ApiModelProperty("银行卡绑定状态")
    private boolean bankState ;

    @ApiModelProperty("是否实名状态")
    private boolean realnameState ;

    @ApiModelProperty("银行密码状态")
    private boolean bankPassworState ;

    @ApiModelProperty("自动投标签约状态")
    private boolean autoTenderState ;

    @ApiModelProperty("自动债权转让签约状态")
    private boolean autoTranferState ;

    @ApiModelProperty("极光推送唯一标识")
    private String  alias ;

    @ApiModelProperty("注册时间")
    private String  registerAt ;

    @ApiModelProperty("投资积分")
    private Long  tenderIntegral ;




}
