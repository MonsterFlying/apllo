package com.gofobao.framework.member.vo.response.pc;

import com.gofobao.framework.core.vo.VoBaseResp;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by admin on 2017/7/19.
 */
@Data
public class VipInfoRes extends VoBaseResp {

    @ApiModelProperty("客服名")
    private String serviceUserName;

    @ApiModelProperty("到期时间")
    private String endAt;

}
