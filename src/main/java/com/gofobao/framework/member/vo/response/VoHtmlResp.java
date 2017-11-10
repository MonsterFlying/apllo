package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Max on 17/5/22.
 */
@ApiModel
@Data
public class VoHtmlResp extends VoBaseResp {

    @ApiModelProperty(value = "请求表单", dataType = "string")
    private String html;
}
