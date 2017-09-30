package com.gofobao.framework.starfire.user.vo.request;

import com.gofobao.framework.starfire.common.request.BaseRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/27.
 */
@Data
public class BindUserModel extends BaseRequest {

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("星火智投用户id")
    private String user_id;

    @ApiModelProperty("对接平台用户id")
    private String platform_uid;

    @ApiModelProperty("用户真实姓名")
    private String user_name;

    @ApiModelProperty("用户身份证号码")
    private String user_identity;

    @ApiModelProperty("请求地址")
    private String bid_url;

    @ApiModelProperty("来源 PC端：1 WAP端：2")
    private String source;


}
