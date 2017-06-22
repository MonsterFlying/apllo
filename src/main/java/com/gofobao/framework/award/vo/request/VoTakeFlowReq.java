package com.gofobao.framework.award.vo.request;

import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Created by admin on 2017/6/22.
 */
@Data
public class VoTakeFlowReq {

    @ApiModelProperty(hidden = true)
    private Long userId;

    @NotNull(message = "流量券ID不能空")
    @Pattern(regexp = RegexHelper.ONLY_IS_NUM,message = "非法请求")
    @ApiModelProperty("流量券id")
    private Long couponId;

}
