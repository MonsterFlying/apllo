package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;

/**
 * Created by Zeke on 2017/5/18.
 */
@Data
@ApiModel
public class VoModifyPasswordReq extends VoBaseReq {
    @ApiModelProperty(name = "当前密码", required = true, dataType = "String")
    private String oldPassword;

    @ApiModelProperty(name = "新密码", required = true, dataType = "String")
    @NotBlank(message = "密码不能为空")
    private String newPassword;

}
