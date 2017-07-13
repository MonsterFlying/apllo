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
    @ApiModelProperty("最高学历 0:小学；1：初中;2:高中;3:中专;4:专科;5:本科;6:研究生;7:硕士;8:博士;9:博士后")
    private Integer education;
    @ApiModelProperty("毕业学校")
    private String school;
    @ApiModelProperty("婚姻状态 已婚未婚 0.保密 1.已婚 2.未婚")
    private Integer maritalStatus;
    @ApiModelProperty("月收入 0：1000元以下;1：1001-2000;2:2001-3000;3:3001-4000;4:4001-5000;5:5001-8000;6:8001-10000;7:10001-30000;8:30001-50000;9:5万以上")
    private Integer graduation;
}
