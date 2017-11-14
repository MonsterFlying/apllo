package com.gofobao.framework.comment.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by xin on 2017/11/8.
 */
@Data
@ApiModel
public class VoUpdateUsernameReq {
    @ApiModelProperty("用户名")
    @NotEmpty(message = "用户名称不能为空")
    private String username  ;
}
