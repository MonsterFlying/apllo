package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 活动首页
 * Created by Administrator on 2017/6/20 0020.
 */
@Data
@ApiModel
public class VoFindIndexResp extends VoBaseResp {
    @ApiModelProperty
    private List<IndexBanner> bannerList = Lists.newArrayList();

    @ApiModelProperty
    private List<FindIndexItem> items = Lists.newArrayList() ;

    @ApiModelProperty
    private List<FindIndexArticle> articles = Lists.newArrayList() ;

}
