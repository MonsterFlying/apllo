package com.gofobao.framework.comment.vo.request;

import com.gofobao.framework.core.vo.VoBaseReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * Created by xin on 2017/11/10.
 */
@Data
@ApiModel
public class VoTopicCommentReq extends VoBaseReq {
    @ApiModelProperty("话题id")
    @NotNull(message = "话题id不能为空")
    private Long topicId;

    @ApiModelProperty("评论内容")
    @NotBlank(message = "内容不能为空")
    @Length(min = 0 , max = 140 , message = "字数不能超过140字")
    private String content;

}
