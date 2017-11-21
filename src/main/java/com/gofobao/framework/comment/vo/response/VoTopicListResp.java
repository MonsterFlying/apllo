package com.gofobao.framework.comment.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by xin on 2017/11/9.
 */
@ApiModel
@Data
public class VoTopicListResp extends VoBaseResp {
    private List<VoTopicResp> voTopicRespList = Lists.newArrayList();
}
