package com.gofobao.framework.starfire.user.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/27.
 */
@Data
public class FetchLoginToken extends BaseRequest {

    @ApiModelProperty("用户手机号")
    private String mobile;

    @ApiModelProperty("星火智投用户id")
    private String user_id;

    @ApiModelProperty("用户id")
    private String platform_uid;

    @ApiModelProperty("用户手机号")
    private String register_token;

    @ApiModelProperty("安全数字签名")
    private String sign;

}
