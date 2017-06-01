package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Zeke on 2017/5/17.
 */
@ApiModel
@Data
public class VoRegisterReq  extends VoBaseReq {
    @ApiModelProperty(value = "注册来源")
    @NotEmpty
    private String source ;

    @ApiModelProperty(value = "注册手机")
    @NotEmpty
    private String phone ;

    @ApiModelProperty(value = "登录密码")
    @NotEmpty
    private String password ;

    @ApiModelProperty(value = "用户名")
    private String userName ;

    @ApiModelProperty(value = "推荐码")
    private String inviteCode ;

}
