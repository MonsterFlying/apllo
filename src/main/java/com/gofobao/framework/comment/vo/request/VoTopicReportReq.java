package com.gofobao.framework.comment.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("举报")
@Data
public class VoTopicReportReq {

    @ApiModelProperty("点赞/取消点赞的来源ID (帖子, 评论, 回复ID)")
    @NotNull(message = "来源编号不能为空")
    private Long soucreId ;


    @ApiModelProperty("点赞/取消点赞的来源类型 (0:主题点赞, 1:评论点赞, 2:回复点赞)")
    @NotNull(message = "来源类型不能为空")
    private Integer sourceType ;

    @ApiModelProperty("举报类型 0: 广告, 1: 政治有害类, 2: 暴恐类. 3:淫秽色情类, 4:赌博类, 5:诈骗类,  6:其他有害类")
    @NotNull(message = "举报类型")
    private Integer reportType ;
}
