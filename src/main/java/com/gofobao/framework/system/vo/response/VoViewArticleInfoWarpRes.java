package com.gofobao.framework.system.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/7.
 */
@Data
public class VoViewArticleInfoWarpRes extends VoBaseResp {

    @ApiModelProperty("内容")
    private String html;

    @ApiModelProperty("標題")
    private String title;

    @ApiModelProperty("時間")
    private String  createAt;
}
