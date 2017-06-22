package com.gofobao.framework.award.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * Created by admin on 2017/6/8.
 */
@Data
@ApiModel("开启红包")
public class VoOpenRedPackageReq {

    @ApiModelProperty(value = "红包id",readOnly = true)
    @Pattern(regexp = RegexHelper.ONLY_IS_NUM,message = "非法访问")
    private Integer redPackageId;

    @ApiModelProperty(hidden = true)
    private Long userId;

}
