package com.gofobao.framework.starfire.user.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/27.
 */
@Data
public class LoginModel extends BaseRequest {

    @ApiModelProperty("登陆token")
    private String login_token;

    @ApiModelProperty("请求的项目地址")
    private String bid_url;

    @ApiModelProperty("请求来源")
    private String source;

    @ApiModelProperty("用户id")
    private String platform_uid;

    @ApiModelProperty("星火智投用户id")
    private String user_id;

    @ApiModelProperty("用户手机号")
    private String mobile;

}
