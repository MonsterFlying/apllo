package com.gofobao.framework.comment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/11/13.
 */
@ApiModel("点赞/取消点赞响应标")
@Data
public class VoTopicTopRecordResp extends VoBaseResp {
    @ApiModelProperty("点赞成功显示 0, 取消点赞显示 1")
    private Integer typeState = 0;
}
