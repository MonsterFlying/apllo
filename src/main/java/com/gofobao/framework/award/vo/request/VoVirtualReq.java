package com.gofobao.framework.award.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * Created by admin on 2017/6/13.
 */
@Data
public class VoVirtualReq {

    @ApiModelProperty(hidden = true)
    private Long userId;
    @ApiModelProperty(readOnly = true, name = "体验金id")
    @Pattern(regexp = RegexHelper.ONLY_IS_NUM, message = "非法访问")
    private Long id;
}
