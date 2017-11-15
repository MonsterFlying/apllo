package com.gofobao.framework.comment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by xin on 2017/11/10.
 */
@Data
@ApiModel
public class VoTopicIntegralListResp extends VoBaseResp {
    @ApiModelProperty("列表")
    private List<VoTopicsIntegralRecord> voTopicCommentItemList = Lists.newArrayList();
}
