package com.gofobao.framework.system.vo.request;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.helper.RegexHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * Created by admin on 2017/7/7.
 */
@Data
public class VoArticleReq extends Page {

    @ApiModelProperty("类型:常见问题:help; 公告:notice; 媒体报道:media; 公司动态:dynamic; 理财百科:wiki; 行业资讯:information; 发现:find;")
    @Pattern(regexp = RegexHelper.ONLY_IS_NUM,message = "类型不能为空,")
    private  String type;
}
