package com.gofobao.framework.comment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/11/13.
 */

@ApiModel("评论回复")
@Data
public class VoTopicReplyResp {
    @ApiModelProperty("用户名")
    private String userName;

    @ApiModelProperty("点赞总数")
    private Integer topTotalNum;

    @ApiModelProperty("回复内容")
    private String content;
}
