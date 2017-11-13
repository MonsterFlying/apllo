package com.gofobao.framework.member.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Created by Zeke on 2017/11/8.
 */
@Data
@ApiModel
public class VoUpdateUserAddress {
    @Autowired
    @NotNull(message = "参数id为必填项")
    private Long id;
    @ApiModelProperty(hidden = true)
    private Long userId;
    @ApiModelProperty("收件人姓名")
    @NotNull(message = "收件人姓名为必填项!")
    private String name;
    @ApiModelProperty("手机号")
    @Pattern(regexp = RegexHelper.REGEX_MOBILE_EXACT, message = "手机号码输入有误!")
    private String phone;
    @ApiModelProperty("国家")
    private String country;
    @ApiModelProperty("省")
    @NotNull(message = "省份为必填项!")
    private String province;
    @ApiModelProperty("市")
    @NotNull(message = "市区为必填项!")
    private String city;
    @ApiModelProperty("地区")
    private String district;
    @NotNull(message = "详细为必填项!")
    @ApiModelProperty("详细地址")
    private String detailedAddress;
}
