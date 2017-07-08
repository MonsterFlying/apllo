package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by admin on 2017/7/7.
 */
@Data
public class VoViewArticleWarpRes extends VoBaseResp {

        private List<ArticleModle> articleModles = Lists.newArrayList();

        @ApiModelProperty("总记录数")
        private Integer totalCount=0;
}
