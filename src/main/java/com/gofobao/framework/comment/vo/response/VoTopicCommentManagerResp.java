package com.gofobao.framework.comment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/11/10.
 */
@Data
@ApiModel
public class VoTopicCommentManagerResp {
    @ApiModelProperty("评论id")
    private Long commentId;

    @ApiModelProperty("回复用户id")
    private Long forUserId;

    @ApiModelProperty("帖子id")
    private Long topicId;

    @ApiModelProperty("评论内容")
    private String content = "";

    @ApiModelProperty("评论者名称")
    private String userName = "";

    @ApiModelProperty("回复时间")
    private String time = "";
}
