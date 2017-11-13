package com.gofobao.framework.comment.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by xin on 2017/11/8.
 */
@Data
@ApiModel
public class VoTopicReq  {
    @ApiModelProperty("板块ID")
    @NotNull(message = "主题板块ID不能为空")
    private Long topicTypeId  ;

    @ApiModelProperty("主题内容")
    @NotBlank(message = "内容不能为空")
    @Length(max = 120,message = "内容最多为140字")
    private String content;

}
