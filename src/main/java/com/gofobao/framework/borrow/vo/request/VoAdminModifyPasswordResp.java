package com.gofobao.framework.borrow.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Zeke on 2017/6/20.
 */
@ApiModel
@Data
public class VoAdminModifyPasswordResp {
    @ApiModelProperty(name = "sign", value = "签名")
    @NotEmpty(message = "验证参数不能为空")
    private String sign;

    @ApiModelProperty("开户主体信息, userId 用户ID")
    @NotEmpty(message = "信息为空")
    private String paramStr;
}
