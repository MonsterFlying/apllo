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
public class VoAdminOpenAccountResp {
    @ApiModelProperty(name = "sign", value = "签名")
    @NotEmpty(message = "验证参数不能为空")
    private String sign;

    @ApiModelProperty("开户主体信息, phone: 开户手机. name: 用户名称, userId: 用户ID, idNo:身份证号, cardNo: 银行卡号")
    @NotEmpty(message = "信息为空")
    private String paramStr;
}
