package com.gofobao.framework.award.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/6/29.
 */
@Data
public class VoViewShareRegiestRes extends VoBaseResp {

    @ApiModelProperty("页面")
    private String html;

    @ApiModelProperty("描述")
    private String desc;

    @ApiModelProperty("二维码地址")
    private String codeUrl;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("请求链接")
    private String requestHtmlUrl;
    @ApiModelProperty("图标")
    private String icon;
}
