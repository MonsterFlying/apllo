package com.gofobao.framework.comment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 */
@Data
@ApiModel
public class VoTopicMemberIntegralResp extends VoBaseResp {
    @ApiModelProperty("可用积分")
    private Long useIntegral = 0L;

    @ApiModelProperty("总积分")
    private Long totalIntegral = 0L;

    @ApiModelProperty("已使用积分")
    private Long noUseIntegral = 0L;
}
