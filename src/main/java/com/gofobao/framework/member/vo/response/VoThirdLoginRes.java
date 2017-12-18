package com.gofobao.framework.member.vo.response;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Zeke on 2017/12/14.
 */
@ApiModel
@Data
public class VoThirdLoginRes extends VoBaseResp{
    @ApiModelProperty("用户登录token")
    private String token;
}
