package com.gofobao.framework.comment.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by xin on 2017/11/10.
 */
@Data
@ApiModel
public class VoTopicCommentItem {

    @ApiModelProperty("评论ID")
    private Long commentId;

    @ApiModelProperty("评论内容")
    private String content = "";

    @ApiModelProperty("评论者名称")
    private String userName = "";

    @ApiModelProperty("用户图像")
    private String userIconUrl = "";

    @ApiModelProperty("点赞总数")
    private String topTotalNum = "";

    @ApiModelProperty("回复总数")
    private String contentTotalNum = "";

    @ApiModelProperty("回复时间")
    private String time = "";

    @ApiModelProperty("点赞状态")
    private boolean topState ;
}
