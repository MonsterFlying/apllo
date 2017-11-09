package com.gofobao.framework.member.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;

/**
 * Created by Zeke on 2017/11/8.
 */
@Data
@ApiModel
public class VoDeleteUserAddress {
    @Autowired
    @NotNull(message = "参数id为必填项")
    private Long id;
    @ApiModelProperty(hidden = true)
    private Long userId;
}
