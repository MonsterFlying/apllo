package com.gofobao.framework.member.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/12.
 */
@Data
public class UserInfoExt extends VoBaseResp {
    @ApiModelProperty("性別 (0、女；1、男；2、保密)")
    private Integer sex;
    @ApiModelProperty("出生年月")
    private String bir;
    @ApiModelProperty("QQ")
    private String qq;
    @ApiModelProperty("常住地址")
    private String address;
    @ApiModelProperty("学历")
    private String educationalHistory;
    @ApiModelProperty("毕业学校")
    private String school;
    @ApiModelProperty("婚姻状态 已婚未婚 0.保密 1.已婚 2.未婚")
    private Integer maritalStatus;
    @ApiModelProperty("收入")
    private String income;
}
