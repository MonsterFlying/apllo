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

    @ApiModelProperty("类型:notice;media;news;help")
    @Pattern(regexp = RegexHelper.ONLY_IS_NUM,message = "类型不能为空,")
    private  String type;
}
