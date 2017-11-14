package com.gofobao.framework.comment.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/11/13.
 */
@Data
@ApiModel("评论回复接口")
public class VoTopicReplyReq {
    @ApiModelProperty("主题id")
    private Long topicId;

    @ApiModelProperty("主题类型id")
    private Long topicTypeId;

    @ApiModelProperty("评论id")
    private Long topicCommentId;

    @ApiModelProperty("回复id")
    private Long topicReplyId;

    @ApiModelProperty("回复内容")
    private String content;

    @ApiModelProperty("回复用户id")
    private long userId;

    @ApiModelProperty("用户图像")
    private String forUserIconUrl;

    @ApiModelProperty("回复类型")
    private Integer replyType;
}
