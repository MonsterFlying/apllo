package com.gofobao.framework.comment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 用户中心
 */
@Data
@ApiModel
public class VoTopicMemberCenterResp extends VoBaseResp {
    @ApiModelProperty("社区昵称")
    private String username = "";

    @ApiModelProperty("头像地址")
    private String avatar = "";

    @ApiModelProperty("帖子最新动态")
    private Integer topicNumber = 0;

    @ApiModelProperty("回复我的帖子")
    private Integer topicReplyNumber = 0;
}
