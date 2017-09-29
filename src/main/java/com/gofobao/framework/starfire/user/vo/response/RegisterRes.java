package com.gofobao.framework.starfire.user.vo.response;

import com.gofobao.framework.starfire.common.response.BaseResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/26.
 */
@Data
public class RegisterRes extends BaseResponse {

    @ApiModelProperty("用户手机号")
    private String mobile;

    @ApiModelProperty("星火智投用户id")
    private String user_id ="";

    @ApiModelProperty("实名认证情况")
    private String RealNameAuthenticResult="";

    @ApiModelProperty("对接平台用户id")
    private String platform_uid="";

    private String register_token="";






















}
