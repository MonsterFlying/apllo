package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@ApiModel
@Data
public class VoBindEmailReq extends VoBaseReq{
    @ApiModelProperty("邮箱")
    @Pattern(regexp = RegexHelper.REGEX_EMAIL, message = "邮箱格式错误")
    private String email ;

    @ApiModelProperty("邮箱验证码")
    @Pattern(regexp = RegexHelper.ONLY_IS_NUM, message = "格式错误" )
    private String emailCode ;
}
