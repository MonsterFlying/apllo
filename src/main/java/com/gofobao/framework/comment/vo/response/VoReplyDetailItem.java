package com.gofobao.framework.comment.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/11/16.
 */
@Data
public class VoReplyDetailItem {

    @ApiModelProperty("评论ID")
    private Long commentId ;

    @ApiModelProperty("回复ID")
    private Long replyId ;

    @ApiModelProperty("回复人")
    private String fromUserName ;

    @ApiModelProperty("回复内容")
    private String content ;

    @ApiModelProperty("被回复人")
    private String toUserName ;
}
