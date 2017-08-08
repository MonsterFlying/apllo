package com.gofobao.framework.asset.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * Created by Max on 17/6/7.
 */
@ApiModel
@Data
public class VoRechargeReq extends VoBaseReq{
    @Min(value = 1, message = "充值金额不能小于1元")
    @ApiModelProperty(required = true, value = "充值金额，必须大于1，小于最大充值额度")
    private Double money ;

    @Pattern(regexp = RegexHelper.REGEX_MOBILE_EXACT)
    @ApiModelProperty(required = true, value = "手机号")
    private String phone ;

    @NotEmpty
    @ApiModelProperty(required = true, value = "短信验证码")
    private String smsCode ;

    @ApiModelProperty(hidden = true)
    private Long userId ;
}
