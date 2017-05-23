package com.gofobao.framework.member.vo;

import com.gofobao.framework.core.vo.VoBaseReq;
import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

/**
 * Created by Max on 17/5/22.
 */
@Data
@ApiModel
public class VoOpenAccountReq extends VoBaseReq{
    @ApiModelProperty(value = "用户真实姓名", required = true, dataType = "String")
    @NotEmpty(message = "真实姓名不能为空")
    private String name ;

    @ApiModelProperty(value = "证件号码，该出请填写18位身份证号" , required = true, dataType = "String")
    @NotEmpty(message = "身份证不能为空")
    @Pattern(regexp = RegexHelper.REGEX_ID_CARD18, message = "身份证号码有误")
    private String idNo ;

    @ApiModelProperty(value = "手机号码" , required = true, dataType = "String")
    @NotEmpty(message = "手机不能为空")
    @Pattern(regexp = RegexHelper.REGEX_MOBILE_EXACT, message = "手机格式不正确")
    private String mobile ;

    @ApiModelProperty(value = "银行卡Id，当从用户银行卡列表选取时，请将选择的银行卡ID填入此处", required = true, dataType = "String")
    private Long bankId ;

    @ApiModelProperty(value = "银行卡号", required = true, dataType = "String")
    @NotEmpty(message = "银行卡不能为空")
    private String cardNo ;

    @ApiModelProperty(value = "短信验证码", required = true, dataType = "String")
    @NotEmpty(message = "短信验证码不能为空")
    private String smsCode ;

}
