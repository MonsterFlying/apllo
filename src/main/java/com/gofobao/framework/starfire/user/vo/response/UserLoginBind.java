package com.gofobao.framework.starfire.user.vo.response;

import com.gofobao.framework.starfire.common.response.BaseResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by master on 2017/9/27.
 */
@Data
public class UserLoginBind extends BaseResponse {

    @ApiModelProperty("渠道编码")
    private String c_code;

    @ApiModelProperty("时间戳")
    private String t_code;

    @ApiModelProperty("用户手机号")
    private String mobile;

    @ApiModelProperty("对接平台用户id")
    private String platform_uid;

    @ApiModelProperty("星火智投用户id")
    private String user_id="";

    @ApiModelProperty("用户在对接平台是否有过投资记录是：true，否：false")
    private String isInvested="";

    @ApiModelProperty("是否是星火智投渠道用户。是：true（仅通过星火渠道绑定平台账号） 否")
    private String isXeenhoChanne="";

    @ApiModelProperty("星火智投用户id")
    private String register_token="";

    @ApiModelProperty("签名")
    private String sign;

    @ApiModelProperty("错误信息，当校验不通过，或绑定不成功时，返回错误信息")
    private String err_msg;


}
