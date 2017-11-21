package com.gofobao.framework.comment.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("点赞业务操作类")
@Data
public class VoTopicTopReq {

    @ApiModelProperty("点赞/取消点赞的来源ID (帖子, 评论, 回复ID)")
    @NotNull(message = "来源编号不能为空")
    private Long soucreId ;


    @ApiModelProperty("点赞/取消点赞的来源类型 (0:主题点赞, 1:评论点赞, 2:回复点赞)")
    @NotNull(message = "来源类型不能为空")
    private Integer sourceType ;
}
