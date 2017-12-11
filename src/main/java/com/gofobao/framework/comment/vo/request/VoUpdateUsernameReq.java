package com.gofobao.framework.comment.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Created by xin on 2017/11/8.
 */
@Data
@ApiModel
public class VoUpdateUsernameReq {
    @ApiModelProperty("用户名")
    @NotEmpty(message = "用户名称不能为空")
    @Pattern(regexp = RegexHelper.REGEX_USERNAME, message = "用户名: 数字, 中文, 英文 1- 12位")
    private String username  ;
}
